export type DifficultyKey = "EASY" | "NORMAL" | "HARD";

export interface GameRuleConfig {
  difficulty: DifficultyKey;
  rows: number;
  cols: number;
  maxTurns: number;
}

export interface CellData {
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
  config: GameRuleConfig;
  path: MatchVertex[];
}

export interface HintResponse {
  success: boolean;
  message: string;
  config: GameRuleConfig;
  hintTiles: MatchVertex[];
}

export interface GameBoardResponse {
  success: boolean;
  message: string;
  map: number[][];
  config: GameRuleConfig;
}
