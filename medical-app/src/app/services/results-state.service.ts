import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ResultsStateService {
  private predictionResult: any = null;

  setPredictionResult(result: any): void {
    this.predictionResult = result;
  }

  getPredictionResult(): any {
    return this.predictionResult;
  }

  clearPredictionResult(): void {
    this.predictionResult = null;
  }
}
