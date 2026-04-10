export interface CellData {
  // 前端棋盘也统一使用 row/col，避免和后端 DTO 的字段语义不一致。
  row: number;
  col: number;
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

export interface HintResponse {
  success: boolean;
  message: string;
  hintTiles: MatchVertex[];
}

export interface GameBoardResponse {
  success: boolean;
  message: string;
  map: number[][];
}
