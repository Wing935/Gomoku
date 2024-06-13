package temo01;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GomokuGame extends JFrame {
    // 常量定义
    private static final int BOARD_SIZE = 15; // 棋盘大小
    private static final int CELL_SIZE = 40; // 每个格子的大小
    private static final int PANEL_WIDTH = BOARD_SIZE * CELL_SIZE; // 面板宽度
    private static final int PANEL_HEIGHT = BOARD_SIZE * CELL_SIZE; // 面板高度
    private static final String EMPTY = "-"; // 空格子
    private static final String PLAYER1 = "黑"; // 玩家1
    private static final String PLAYER2 = "白"; // 玩家2

    // 变量定义
    private String[][] board; // 棋盘
    private boolean player1Turn; // 当前是否为玩家1的回合
    private boolean gameOver; // 游戏是否结束
    private boolean selfPlayMode; // 是否为自我对弈模式
    private boolean computerPlayMode; // 是否为电脑对弈模式
    private JLabel statusBar; // 状态栏
    private BoardPanel boardPanel; // 棋盘面板

    // 构造函数
    public GomokuGame() {
        // 初始化棋盘
        board = new String[BOARD_SIZE][BOARD_SIZE];
        player1Turn = true; // 初始为玩家1的回合
        gameOver = false; // 游戏未结束
        selfPlayMode = false; // 初始为非自我对弈模式
        computerPlayMode = false; // 初始为非电脑对弈模式
        initializeBoard(); // 初始化棋盘

        // 创建棋盘面板并添加到窗口中心
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // 创建状态栏并添加到窗口底部
        statusBar = new JLabel("请玩家 " + PLAYER1 + " 落子");
        add(statusBar, BorderLayout.SOUTH);

        // 创建控制面板并设置布局
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 3));

        // 自我对弈按钮
        JButton selfPlayButton = new JButton("双人对弈模式");
        selfPlayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selfPlayMode = !selfPlayMode;
                computerPlayMode = false; // 禁用电脑对弈模式
                resetGame(); // 切换模式时重置游戏
                if (selfPlayMode) {
                    statusBar.setText("双人对弈模式: 请玩家 " + PLAYER1 + " 落子");
                } else {
                    statusBar.setText("请玩家 " + PLAYER1 + " 落子");
                }
            }
        });
        controlPanel.add(selfPlayButton);

        // 电脑对弈按钮
        JButton computerPlayButton = new JButton("人机对弈模式");
        computerPlayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computerPlayMode = !computerPlayMode;
                selfPlayMode = false; // 禁用自我对弈模式
                resetGame(); // 切换模式时重置游戏
                if (computerPlayMode) {
                    statusBar.setText("人机对弈模式: 请玩家 " + PLAYER1 + " 落子");
                } else {
                    statusBar.setText("请玩家 " + PLAYER1 + " 落子");
                }
            }
        });
        controlPanel.add(computerPlayButton);

        // 重置按钮
        JButton resetButton = new JButton("重置棋盘");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        controlPanel.add(resetButton);

        // 将控制面板添加到窗口顶部
        add(controlPanel, BorderLayout.NORTH);

        // 为棋盘面板添加鼠标点击事件监听器
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) return; // 如果游戏结束则不响应点击

                int x = e.getX();
                int y = e.getY();
                int col = x / CELL_SIZE;
                int row = y / CELL_SIZE;

                if (isValidMove(row, col)) {
                    board[row][col] = player1Turn ? PLAYER1 : PLAYER2;
                    boardPanel.repaint();
                    if (checkWin(row, col)) {
                        gameOver = true;
                        statusBar.setText("玩家 " + (player1Turn ? PLAYER1 : PLAYER2) + " 胜利!");
                    } else if (isBoardFull()) {
                        gameOver = true;
                        statusBar.setText("平局！");
                    } else {
                        player1Turn = !player1Turn;
                        if (selfPlayMode) {
                            statusBar.setText("双人对弈模式: 请玩家 " + (player1Turn ? PLAYER1 : PLAYER2) + " 落子");
                        } else if (computerPlayMode && !player1Turn) {
                            statusBar.setText("人机对弈模式: 电脑落子");
                            computerMove();
                        } else {
                            statusBar.setText("请玩家 " + (player1Turn ? PLAYER1 : PLAYER2) + " 落子");
                        }
                    }
                }
            }
        });

        // 设置窗口大小和关闭操作
        setSize(PANEL_WIDTH + 16, PANEL_HEIGHT + 60 + 39); // 增加窗口的宽度和高度，以确保边界完整显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("五子棋");
        setVisible(true);
    }

    // 棋盘面板类，继承自 JPanel
    class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 设置背景色为黄色
            setBackground(Color.YELLOW);

            // 绘制棋子
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;
                    if (board[row][col].equals(PLAYER1)) {
                        g.setColor(Color.BLACK);
                        g.fillOval(x, y, CELL_SIZE, CELL_SIZE);
                    } else if (board[row][col].equals(PLAYER2)) {
                        g.setColor(Color.WHITE);
                        g.fillOval(x, y, CELL_SIZE, CELL_SIZE);
                    }
                    g.setColor(Color.BLACK);
                    g.drawOval(x, y, CELL_SIZE, CELL_SIZE);
                }
            }

            // 绘制棋盘网格
            for (int i = 0; i <= BOARD_SIZE; i++) {
                g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
                g.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
        }
    }

    // 初始化棋盘，将所有格子设置为空
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    // 检查移动是否有效
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col].equals(EMPTY);
    }

    // 检查是否获胜
    private boolean checkWin(int row, int col) {
        String player = player1Turn ? PLAYER1 : PLAYER2;

        // 检查水平方向
        if (checkLine(row, col, 0, 1, player) >= 5) return true;

        // 检查垂直方向
        if (checkLine(row, col, 1, 0, player) >= 5) return true;

        // 检查左上到右下对角线
        if (checkLine(row, col, 1, 1, player) >= 5) return true;

        // 检查右上到左下对角线
        if (checkLine(row, col, 1, -1, player) >= 5) return true;

        return false;
    }

    // 检查某一方向上的连续棋子数
    private int checkLine(int row, int col, int dRow, int dCol, String player) {
        int count = 1; // 当前落子已经算一个
        int r, c;

        // 检查正方向
        r = row + dRow;
        c = col + dCol;
        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c].equals(player)) {
            count++;
            r += dRow;
            c += dCol;
        }

        // 检查反方向
        r = row - dRow;
        c = col - dCol;
        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c].equals(player)) {
            count++;
            r -= dRow;
            c -= dCol;
        }

        return count;
    }

    // 检查棋盘是否已满
    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].equals(EMPTY)) {
                    return false;
                }
            }
        }
        return true;
    }

    // 电脑走棋
    private void computerMove() {
        int[] move = minimax(2, Integer.MIN_VALUE, Integer.MAX_VALUE, player1Turn);
        board[move[1]][move[2]] = PLAYER2;
        boardPanel.repaint();
        if (checkWin(move[1], move[2])) {
            gameOver = true;
            statusBar.setText("玩家 " + PLAYER2 + " 胜利!");
        } else if (isBoardFull()) {
            gameOver = true;
            statusBar.setText("平局！");
        } else {
            player1Turn = true;
            statusBar.setText("请玩家 " + PLAYER1 + " 落子");
        }
    }

    // Minimax算法，用于电脑决策
    private int[] minimax(int depth, int alpha, int beta, boolean maximizingPlayer) {
        int[] bestMove = new int[3];
        String player = maximizingPlayer ? PLAYER2 : PLAYER1;

        if (depth == 0 || checkWin(0, 0) || checkWin(0, 1)) {
            int score = evaluate();
            bestMove[0] = score;
            return bestMove;
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j].equals(EMPTY)) {
                        board[i][j] = player;
                        int eval = minimax(depth - 1, alpha, beta, false)[0];
                        board[i][j] = EMPTY;
                        if (eval > maxEval) {
                            maxEval = eval;
                            bestMove[0] = maxEval;
                            bestMove[1] = i;
                            bestMove[2] = j;
                        }
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return bestMove;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j].equals(EMPTY)) {
                        board[i][j] = player;
                        int eval = minimax(depth - 1, alpha, beta, true)[0];
                        board[i][j] = EMPTY;
                        if (eval < minEval) {
                            minEval = eval;
                            bestMove[0] = minEval;
                            bestMove[1] = i;
                            bestMove[2] = j;
                        }
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return bestMove;
        }
    }

    // 评估棋盘分数
    private int evaluate() {
        int score = 0;
        String player = player1Turn ? PLAYER1 : PLAYER2;

        // 评估水平方向
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col <= BOARD_SIZE - 5; col++) {
                int count = 0;
                for (int i = 0; i < 5; i++) {
                    if (board[row][col + i].equals(player)) {
                        count++;
                    } else if (board[row][col + i] != EMPTY) {
                        count = 0;
                        break;
                    }
                }
                score += count == 1 ? 1 : count == 2 ? 10 : count == 3 ? 100 : count == 4 ? 1000 : 0;
            }
        }

        // 评估垂直方向
        for (int col = 0; col < BOARD_SIZE; col++) {
            for (int row = 0; row <= BOARD_SIZE - 5; row++) {
                int count = 0;
                for (int i = 0; i < 5; i++) {
                    if (board[row + i][col].equals(player)) {
                        count++;
                    } else if (board[row + i][col] != EMPTY) {
                        count = 0;
                        break;
                    }
                }
                score += count == 1 ? 1 : count == 2 ? 10 : count == 3 ? 100 : count == 4 ? 1000 : 0;
            }
        }

        // 评估左上到右下对角线
        for (int row = 0; row <= BOARD_SIZE - 5; row++) {
            for (int col = 0; col <= BOARD_SIZE - 5; col++) {
                int count = 0;
                for (int i = 0; i < 5; i++) {
                    if (board[row + i][col + i].equals(player)) {
                        count++;
                    } else if (board[row + i][col + i] != EMPTY) {
                        count = 0;
                        break;
                    }
                }
                score += count == 1 ? 1 : count == 2 ? 10 : count == 3 ? 100 : count == 4 ? 1000 : 0;
            }
        }

        // 评估右上到左下对角线
        for (int row = 0; row <= BOARD_SIZE - 5; row++) {
            for (int col = 4; col < BOARD_SIZE; col++) {
                int count = 0;
                for (int i = 0; i < 5; i++) {
                    if (board[row + i][col - i].equals(player)) {
                        count++;
                    } else if (board[row + i][col - i] != EMPTY) {
                        count = 0;
                        break;
                    }
                }
                score += count == 1 ? 1 : count == 2 ? 10 : count == 3 ? 100 : count == 4 ? 1000 : 0;
            }
        }

        return score;
    }

    // 重置游戏
    private void resetGame() {
        initializeBoard();
        player1Turn = true;
        gameOver = false;
        statusBar.setText("请玩家 " + PLAYER1 + " 落子");
        boardPanel.repaint();
    }

    // 主函数，启动游戏
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GomokuGame();
            }
        });
    }
}
