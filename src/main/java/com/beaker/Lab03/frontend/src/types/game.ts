export interface CellData {
  x: number;
  y: number;
  type: number;
  isEmpty: boolean;
  isSelected: boolean;
}

export interface MatchVertex {
  row: number;
  col: number;
  type: number;
}

export interface MatchCheckResponse {
  connected: boolean;
  message: string;
  map: number[][];
  path: MatchVertex[];
}

export interface GameBoardResponse {
  success: boolean;
  message: string;
  map: number[][];
}
