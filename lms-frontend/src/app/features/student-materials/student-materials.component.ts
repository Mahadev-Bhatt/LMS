import { Component, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { LearningMaterialService } from '../../core/services/learning-material.service';
import { LearningMaterial } from '../../core/models/models';
import { environment } from '../../../environments/environment';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-student-materials',
  templateUrl: './student-materials.component.html',
  styleUrls: ['./student-materials.component.css']
})
export class StudentMaterialsComponent implements OnInit, OnDestroy {
  materials: LearningMaterial[] = [];
  filtered: LearningMaterial[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  courseFilter = 'all';
  activeMaterial: LearningMaterial | null = null;
  activePreviewUrl: SafeResourceUrl | null = null;
  pdfSecondsRemaining = 120;
  completionSaving = false;
  completionError = false;
  previewLoading = false;
  previewError = '';

  get pdfMinutesRemaining(): number {
    return Math.floor(this.pdfSecondsRemaining / 60);
  }

  private pdfTimer: ReturnType<typeof setInterval> | null = null;
  private youtubePollTimer: ReturnType<typeof setInterval> | null = null;
  private youtubeFrame: HTMLIFrameElement | null = null;
  private pdfLoadSubscription: Subscription | null = null;
  private pdfObjectUrl: string | null = null;
  private youtubePlaying = false;
  private youtubeDuration = 0;
  private youtubeWatchedSeconds = new Set<number>();
  private lastYoutubeSample: { currentTime: number; sampledAt: number } | null = null;
  private readonly pdfSecondsViewed = new Map<number, number>();
  private readonly completionReady = new Set<number>();
  private readonly completionFailed = new Set<number>();

  constructor(
    private materialService: LearningMaterialService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    window.addEventListener('message', this.handleYoutubeMessage);
    document.addEventListener('visibilitychange', this.handleVisibilityChange);
    this.load();
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.handleYoutubeMessage);
    document.removeEventListener('visibilitychange', this.handleVisibilityChange);
    this.closePreview();
  }

  load(): void {
    this.loading = true;
    this.error = false;
    this.materialService.getMine().subscribe({
      next: materials => {
        this.materials = materials;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    const query = this.searchTerm.trim().toLowerCase();
    this.filtered = this.materials.filter(material =>
      (!query || material.title.toLowerCase().includes(query)
        || (material.description ?? '').toLowerCase().includes(query))
      && (this.courseFilter === 'all' || material.courseId === Number(this.courseFilter)));
  }

  get courses(): { id: number; title: string }[] {
    return [...new Map(this.materials.map(material =>
      [material.courseId, { id: material.courseId, title: material.courseTitle ?? 'Course' }])).values()];
  }

  canPreview(material: LearningMaterial): boolean {
    return this.getPreviewUrl(material) !== null;
  }

  showPreview(material: LearningMaterial): void {
    const previewUrl = this.getPreviewUrl(material);
    if (!previewUrl) {
      return;
    }

    this.stopPreviewTracking();
    this.activeMaterial = material;
    this.activePreviewUrl = null;
    this.previewLoading = this.isUploadedPdf(material);
    this.previewError = '';
    this.completionError = false;
    this.pdfSecondsRemaining = Math.max(0, 120 - this.getPdfSecondsViewed(material));

    if (this.isUploadedPdf(material)) {
      this.pdfLoadSubscription = this.materialService.getPdfFile(material.resourceUrl).subscribe({
        next: pdf => {
          if (this.activeMaterial !== material) {
            return;
          }
          this.pdfObjectUrl = URL.createObjectURL(pdf);
          this.activePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.pdfObjectUrl);
          this.previewLoading = false;
        },
        error: error => {
          if (this.activeMaterial !== material) {
            return;
          }
          this.previewLoading = false;
          this.previewError = error.status === 403
            ? 'You do not have access to this course PDF. Check your active course enrollment.'
            : 'The PDF could not be loaded from the server. Check that the backend is running and the PDF is available.';
        }
      });
    } else {
      this.activePreviewUrl = previewUrl;
    }
  }

  closePreview(): void {
    this.stopPreviewTracking();
    this.pdfLoadSubscription?.unsubscribe();
    this.pdfLoadSubscription = null;
    if (this.pdfObjectUrl) {
      URL.revokeObjectURL(this.pdfObjectUrl);
      this.pdfObjectUrl = null;
    }
    this.activeMaterial = null;
    this.activePreviewUrl = null;
    this.completionError = false;
    this.previewLoading = false;
    this.previewError = '';
  }

  resourceHref(material: LearningMaterial): string {
    return new URL(material.resourceUrl, environment.apiUrl).toString();
  }

  onPreviewLoaded(event: Event, material: LearningMaterial): void {
    const frame = event.target;
    if (!(frame instanceof HTMLIFrameElement) || material.completed) {
      return;
    }

    if (this.isYouTubeMaterial(material)) {
      this.youtubeFrame = frame;
      this.startYouTubeTracking();
    } else if (this.isPdfMaterial(material)) {
      this.startPdfTracking(material);
    }
  }

  isPdfMaterial(material: LearningMaterial): boolean {
    return !this.isYouTubeMaterial(material)
      && (material.materialType === 'DOCUMENT' || this.getResourceUrl(material)?.pathname.toLowerCase().endsWith('.pdf') === true);
  }

  retryCompletion(material: LearningMaterial): void {
    if (material.id !== undefined && this.completionReady.has(material.id)) {
      this.completionFailed.delete(material.id);
      this.markCompleted(material);
    }
  }

  private getPreviewUrl(material: LearningMaterial): SafeResourceUrl | null {
    let url = this.getResourceUrl(material);
    if (!url) {
      return null;
    }

    const youtubeEmbedUrl = this.getYouTubeEmbedUrl(url);
    if (youtubeEmbedUrl) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(youtubeEmbedUrl);
    }

    if (material.materialType !== 'DOCUMENT' && !url.pathname.toLowerCase().endsWith('.pdf')) {
      return null;
    }

    const drivePreviewUrl = this.getGoogleDrivePreviewUrl(url);
    if (drivePreviewUrl) {
      url = drivePreviewUrl;
    } else {
      const docsMatch = url.hostname === 'docs.google.com'
        ? url.pathname.match(/^\/(document|presentation|spreadsheets)\/d\/([^/]+)/)
        : null;
      if (docsMatch) {
        url = new URL(
          `https://docs.google.com/${docsMatch[1]}/d/${encodeURIComponent(docsMatch[2])}/preview`
        );
      }
    }

    return this.sanitizer.bypassSecurityTrustResourceUrl(url.toString());
  }

  private getResourceUrl(material: LearningMaterial): URL | null {
    try {
      const url = new URL(material.resourceUrl, environment.apiUrl);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url : null;
    } catch {
      return null;
    }
  }

  private isYouTubeMaterial(material: LearningMaterial): boolean {
    const url = this.getResourceUrl(material);
    return url !== null && this.getYouTubeVideoId(url) !== null;
  }

  private isUploadedPdf(material: LearningMaterial): boolean {
    const url = this.getResourceUrl(material);
    return url !== null
      && url.origin === new URL(environment.apiUrl).origin
      && /^\/api\/materials\/\d+\/file$/.test(url.pathname);
  }

  private getGoogleDrivePreviewUrl(url: URL): URL | null {
    if (url.hostname !== 'drive.google.com') {
      return null;
    }

    const fileId = url.pathname.match(/^\/file\/d\/([^/]+)/)?.[1]
      ?? url.searchParams.get('id');
    return fileId
      ? new URL(`https://drive.google.com/file/d/${encodeURIComponent(fileId)}/preview`)
      : null;
  }

  private getYouTubeEmbedUrl(url: URL): string | null {
    const videoId = this.getYouTubeVideoId(url);
    return videoId
      ? `https://www.youtube-nocookie.com/embed/${videoId}?enablejsapi=1&origin=${encodeURIComponent(window.location.origin)}`
      : null;
  }

  private getYouTubeVideoId(url: URL): string | null {
    const hostname = url.hostname.toLowerCase();
    let videoId: string | undefined;

    if (hostname === 'youtu.be' || hostname === 'www.youtu.be') {
      videoId = url.pathname.split('/').filter(Boolean)[0];
    } else if (['youtube.com', 'www.youtube.com', 'm.youtube.com'].includes(hostname)) {
      const pathParts = url.pathname.split('/').filter(Boolean);
      videoId = url.searchParams.get('v') ?? (
        ['embed', 'shorts', 'live'].includes(pathParts[0]) ? pathParts[1] : undefined
      );
    }

    return videoId && /^[\w-]{11}$/.test(videoId) ? videoId : null;
  }

  private startPdfTracking(material: LearningMaterial): void {
    if (!material.id || material.completed) {
      return;
    }

    this.pdfTimer = setInterval(() => {
      if (document.visibilityState !== 'visible' || this.activeMaterial !== material) {
        return;
      }

      const secondsViewed = this.getPdfSecondsViewed(material) + 1;
      this.pdfSecondsViewed.set(material.id!, secondsViewed);
      this.pdfSecondsRemaining = Math.max(0, 120 - secondsViewed);
      if (secondsViewed >= 120) {
        this.completionReady.add(material.id!);
        this.markCompleted(material);
      }
    }, 1000);
  }

  private startYouTubeTracking(): void {
    this.youtubeFrame?.contentWindow?.postMessage(
      JSON.stringify({ event: 'listening', id: 'lms-player' }),
      'https://www.youtube-nocookie.com'
    );
    this.sendYouTubeCommand('addEventListener', ['onStateChange']);
    this.youtubePollTimer = setInterval(() => {
      this.sendYouTubeCommand('getCurrentTime');
      this.sendYouTubeCommand('getDuration');
      this.sendYouTubeCommand('getPlayerState');
    }, 1000);
  }

  private readonly handleYoutubeMessage = (event: MessageEvent): void => {
    if (!this.youtubeFrame || event.source !== this.youtubeFrame.contentWindow
        || !['https://www.youtube-nocookie.com', 'https://www.youtube.com'].includes(event.origin)) {
      return;
    }

    let data: { event?: string; info?: { currentTime?: number; duration?: number; playerState?: number } };
    try {
      data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
    } catch {
      return;
    }

    if (data?.event !== 'infoDelivery' || !data.info) {
      return;
    }

    const { currentTime, duration, playerState } = data.info;
    if (typeof duration === 'number' && duration > 0) {
      this.youtubeDuration = duration;
    }
    if (typeof playerState === 'number') {
      this.youtubePlaying = playerState === 1;
    }
    const videoEnded = playerState === 0;
    if (typeof currentTime !== 'number' || (!this.youtubePlaying && !videoEnded)) {
      this.lastYoutubeSample = null;
      return;
    }

    const sampledAt = Date.now();
    if (this.lastYoutubeSample) {
      const elapsedSeconds = (sampledAt - this.lastYoutubeSample.sampledAt) / 1000;
      const playedSeconds = currentTime - this.lastYoutubeSample.currentTime;
      if (playedSeconds >= 0 && playedSeconds <= elapsedSeconds * 2.25 + 0.75) {
        const firstSecond = Math.floor(this.lastYoutubeSample.currentTime);
        const lastSecond = Math.min(Math.floor(currentTime), Math.ceil(this.youtubeDuration) - 1);
        for (let second = firstSecond; second <= lastSecond; second++) {
          this.youtubeWatchedSeconds.add(second);
        }
      }
    } else if (this.youtubePlaying && currentTime <= 1.5) {
      this.youtubeWatchedSeconds.add(0);
    }
    this.lastYoutubeSample = { currentTime, sampledAt };

    const requiredSeconds = Math.ceil(this.youtubeDuration);
    const hasReachedEnd = currentTime >= this.youtubeDuration - 0.5 || playerState === 0;
    if (this.youtubeDuration > 0 && hasReachedEnd
        && this.youtubeWatchedSeconds.size >= requiredSeconds && this.activeMaterial) {
      if (this.activeMaterial.id !== undefined) {
        this.completionReady.add(this.activeMaterial.id);
      }
      this.markCompleted(this.activeMaterial);
    }
  };

  private readonly handleVisibilityChange = (): void => {
    if (document.visibilityState !== 'visible') {
      this.lastYoutubeSample = null;
    }
  };

  private sendYouTubeCommand(func: string, args: unknown[] = []): void {
    this.youtubeFrame?.contentWindow?.postMessage(
      JSON.stringify({ event: 'command', func, args, id: 'lms-player' }),
      'https://www.youtube-nocookie.com'
    );
  }

  private getPdfSecondsViewed(material: LearningMaterial): number {
    return material.id === undefined ? 0 : this.pdfSecondsViewed.get(material.id) ?? 0;
  }

  private markCompleted(material: LearningMaterial): void {
    if (material.completed || this.completionSaving) {
      return;
    }
    if (material.id === undefined) {
      this.completionError = true;
      return;
    }
    if (this.completionFailed.has(material.id)) {
      return;
    }

    this.completionSaving = true;
    this.completionError = false;
    this.materialService.markCompleted(material.id).subscribe({
      next: () => {
        material.completed = true;
        this.completionSaving = false;
        this.completionError = false;
        this.stopPreviewTracking();
      },
      error: () => {
        this.completionSaving = false;
        this.completionError = true;
        this.completionFailed.add(material.id!);
      }
    });
  }

  private stopPreviewTracking(): void {
    if (this.pdfTimer) {
      clearInterval(this.pdfTimer);
      this.pdfTimer = null;
    }
    if (this.youtubePollTimer) {
      clearInterval(this.youtubePollTimer);
      this.youtubePollTimer = null;
    }
    this.youtubeFrame = null;
    this.youtubePlaying = false;
    this.youtubeDuration = 0;
    this.youtubeWatchedSeconds.clear();
    this.lastYoutubeSample = null;
  }
}
