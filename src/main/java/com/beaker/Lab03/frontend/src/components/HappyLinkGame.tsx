import { useMemo, useState } from "react";

const ROWS = 10;
const COLS = 16;
const PAIR_COUNT = (ROWS * COLS) / 2;
const ICON_SET = ["🍒", "🍋", "🍇", "🍉", "🍑", "🥝", "🍍", "🍓"];

export interface CellData {
  x: number;
  y: number;
  type: number;
  isEmpty: boolean;
  isSelected: boolean;
}

function shuffle<T>(items: T[]) {
  const next = [...items];

  for (let index = next.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(Math.random() * (index + 1));
    [next[index], next[randomIndex]] = [next[randomIndex], next[index]];
  }

  return next;
}

function createInitialBoard(): CellData[][] {
  const pairTypes = Array.from({ length: PAIR_COUNT }, (_, index) => index % ICON_SET.length);
  const shuffledTypes = shuffle([...pairTypes, ...pairTypes]);

  return Array.from({ length: ROWS }, (_, x) =>
    Array.from({ length: COLS }, (_, y) => {
      const type = shuffledTypes[x * COLS + y];

      return {
        x,
        y,
        type,
        isEmpty: false,
        isSelected: false
      };
    })
  );
}

function updateCell(
  board: CellData[][],
  target: Pick<CellData, "x" | "y">,
  updates: Partial<CellData>
) {
  return board.map((row, rowIndex) =>
    row.map((cell, colIndex) =>
      rowIndex === target.x && colIndex === target.y ? { ...cell, ...updates } : cell
    )
  );
}

export function HappyLinkGame() {
  const [board, setBoard] = useState<CellData[][]>(() => createInitialBoard());
  const [selectedCells, setSelectedCells] = useState<CellData[]>([]);

  const selectedCount = selectedCells.length;
  const totalCells = useMemo(() => ROWS * COLS, []);

  const handleStartGame = () => {
    setBoard(createInitialBoard());
    setSelectedCells([]);
  };

  const clearSelectedState = (cells: CellData[]) => {
    setBoard((currentBoard) =>
      cells.reduce(
        (nextBoard, cell) => updateCell(nextBoard, cell, { isSelected: false }),
        currentBoard
      )
    );
    setSelectedCells([]);
  };

  const handleMatchCheck = async (cell1: CellData, cell2: CellData) => {
    // TODO: 发送 fetch 请求给 Spring Boot 后端进行连通判断
    console.log("Pending backend match check:", { cell1, cell2 });

    await Promise.resolve();
    clearSelectedState([cell1, cell2]);
  };

  const handleCellClick = (cell: CellData) => {
    if (cell.isEmpty || cell.isSelected || selectedCells.length >= 2) {
      return;
    }

    const nextSelectedCells = [...selectedCells, { ...cell, isSelected: true }];

    setBoard((currentBoard) => updateCell(currentBoard, cell, { isSelected: true }));
    setSelectedCells(nextSelectedCells);

    if (nextSelectedCells.length === 2) {
      void handleMatchCheck(nextSelectedCells[0], nextSelectedCells[1]);
    }
  };

  return (
    <main className="min-h-screen overflow-x-auto bg-space-grid px-4 py-8 text-slate-50">
      <div className="mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-[1440px] flex-col gap-8 rounded-[32px] border border-white/15 bg-white/10 p-6 shadow-glass backdrop-blur-2xl lg:p-10">
        <header className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-3">
            <p className="text-sm uppercase tracking-[0.4em] text-aurora-400/90">
              Glassmorphism Arcade
            </p>
            <div>
              <h1 className="text-4xl font-black tracking-[0.18em] text-white md:text-5xl">
                欢乐连连看
              </h1>
              <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-200/85 md:text-base">
                经典实验项目的 Web 版重构，采用 10 x 16 固定棋盘、毛玻璃卡片和现代化交互反馈。
              </p>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center">
            <div className="rounded-2xl border border-white/15 bg-slate-950/25 px-4 py-3 text-sm text-slate-100/90 backdrop-blur-xl">
              已选中 <span className="font-bold text-aurora-400">{selectedCount}</span> / 2
              <span className="mx-2 text-white/25">|</span>
              总格子 <span className="font-bold text-cyan-300">{totalCells}</span>
            </div>
            <button
              type="button"
              onClick={handleStartGame}
              className="rounded-2xl border border-emerald-200/40 bg-gradient-to-r from-emerald-300/90 via-cyan-300/90 to-sky-300/90 px-6 py-4 text-base font-bold text-slate-950 shadow-[0_12px_40px_rgba(94,234,212,0.35)] transition duration-200 hover:scale-[1.02] hover:shadow-[0_18px_50px_rgba(103,232,249,0.45)] focus:outline-none focus:ring-4 focus:ring-emerald-200/60"
            >
              开始游戏 / 重新开始
            </button>
          </div>
        </header>

        <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
          <div className="rounded-[28px] border border-white/15 bg-slate-950/20 p-4 backdrop-blur-xl md:p-5">
            <div
              className="grid gap-2 md:gap-2.5"
              style={{
                gridTemplateRows: `repeat(${ROWS}, minmax(0, 1fr))`,
                gridTemplateColumns: `repeat(${COLS}, minmax(0, 1fr))`
              }}
            >
              {board.flat().map((cell) => {
                const icon = ICON_SET[cell.type % ICON_SET.length];

                return (
                  <button
                    key={`${cell.x}-${cell.y}`}
                    type="button"
                    onClick={() => handleCellClick(cell)}
                    className={[
                      "aspect-square rounded-2xl border text-xl transition duration-200 md:text-2xl",
                      "backdrop-blur-xl focus:outline-none focus:ring-2 focus:ring-cyan-200/70",
                      cell.isEmpty
                        ? "border-white/5 bg-white/5 text-transparent"
                        : "border-white/15 bg-white/14 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.16)] hover:scale-[1.05] hover:border-cyan-200/50 hover:bg-white/18",
                      cell.isSelected ? "shadow-glow ring-2 ring-emerald-200/80" : ""
                    ].join(" ")}
                    aria-label={`第${cell.x + 1}行 第${cell.y + 1}列`}
                  >
                    <span className="drop-shadow-[0_4px_18px_rgba(255,255,255,0.28)]">{icon}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <aside className="space-y-4 rounded-[28px] border border-white/15 bg-slate-950/25 p-6 backdrop-blur-xl">
            <div>
              <p className="text-xs uppercase tracking-[0.35em] text-cyan-200/70">Game Notes</p>
              <h2 className="mt-2 text-2xl font-bold text-white">核心规则</h2>
            </div>
            <div className="space-y-3 text-sm leading-6 text-slate-200/85">
              <p>棋盘固定为 10 行 16 列，使用 CSS Grid 严格布局。</p>
              <p>当前前端只处理选中状态与交互表现，连通性判断交给 Spring Boot 后端。</p>
              <p>当玩家恰好选中 2 个方块时，会触发异步校验入口 `handleMatchCheck`。</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-4 text-sm text-slate-100/90">
              接口占位已保留，后续可直接在 `handleMatchCheck` 中发起 `/api` 请求。
            </div>
          </aside>
        </section>
      </div>
    </main>
  );
}
