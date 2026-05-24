package com.example.autotarget;

import android.view.View;
import android.graphics.Paint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * View customizada que renderiza o jogo no Canvas.
 * Desenha alvos, canhões e projéteis.
 */
public class JogoView extends View {

    private Jogo jogo;
    private Paint paintAlvo;
    private Paint paintCanhao;
    private Paint paintProjetil;
    private Paint paintTexto;
    private Paint paintDebug;
    private boolean podeDesenhar;

    // Variáveis para cálculo de FPS
    private long lastTime = 0;
    private int frameCount = 0;
    private int currentFps = 0;

    public JogoView(Context context) {
        super(context);
        init();
    }

    public JogoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JogoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Inicializa as propriedades da view.
     */
    private void init() {
        paintAlvo = new Paint();
        paintAlvo.setColor(Color.BLUE);
        paintAlvo.setStyle(Paint.Style.FILL);

        paintCanhao = new Paint();
        paintCanhao.setColor(Color.GREEN);
        paintCanhao.setStyle(Paint.Style.STROKE);
        paintCanhao.setStrokeWidth(5);

        paintProjetil = new Paint();
        paintProjetil.setColor(Color.RED);
        paintProjetil.setStyle(Paint.Style.FILL);

        paintTexto = new Paint();
        paintTexto.setColor(Color.WHITE);
        paintTexto.setTextSize(60);
        paintTexto.setStyle(Paint.Style.FILL);

        paintDebug = new Paint();
        paintDebug.setColor(Color.YELLOW);
        paintDebug.setTextSize(30);
        paintDebug.setStyle(Paint.Style.FILL);

        podeDesenhar = true;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (jogo != null) {
            jogo.setDimensoes(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (jogo == null || !podeDesenhar) {
            return;
        }

        // Cálculo de FPS
        long now = System.currentTimeMillis();
        frameCount++;
        if (now - lastTime >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            lastTime = now;
            GerenciadorMetricas.registrarFPS(currentFps);
        }

        // Desenha fundo
        canvas.drawColor(Color.BLACK);

        // Desenha linha divisória
        Paint linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha(50);
        linePaint.setStrokeWidth(3);
        canvas.drawLine(getWidth() / 2f, 0, getWidth() / 2f, getHeight(), linePaint);

        // Desenha alvos
        for (Alvo alvo : jogo.getAlvos()) {
            if (alvo instanceof AlvoRapido) {
                paintAlvo.setColor(Color.YELLOW);
            } else {
                paintAlvo.setColor(Color.BLUE);
            }
            canvas.drawCircle((float) alvo.getX(), (float) alvo.getY(),
                    (float) alvo.getRaio(), paintAlvo);
        }

        // Desenha canhões
        for (Canhao canhao : jogo.getCanhoes()) {
            drawCanhao(canvas, canhao);
        }

        // Desenha projéteis
        for (Canhao canhao : jogo.getCanhoes()) {
            for (Projetil projetil : canhao.getProjeteis()) {
                canvas.drawCircle((float) projetil.getX(), (float) projetil.getY(),
                        (float) projetil.getRaio(), paintProjetil);
            }
        }

        // Desenha placar
        canvas.drawText("Abates: " + jogo.getAbatesTotal(), 40, 100, paintTexto);

        // Informações de DEBUG (apenas se ativado)
        if (GerenciadorMetricas.DEBUG) {
            canvas.drawText("FPS: " + currentFps, 40, 150, paintDebug);
            canvas.drawText("Alvos: " + jogo.getAlvos().size(), 40, 190, paintDebug);
        }

        invalidate();
    }

    private void drawCanhao(Canvas canvas, Canhao canhao) {
        float x = (float) canhao.getX();
        float y = (float) canhao.getY();
        float angulo = (float) canhao.getAngulo();
        float tamanho = 50;

        float x1 = x + tamanho * (float) Math.cos(angulo);
        float y1 = y + tamanho * (float) Math.sin(angulo);

        float x2 = x + tamanho * (float) Math.cos(angulo + 2.094);
        float y2 = y + tamanho * (float) Math.sin(angulo + 2.094);

        float x3 = x + tamanho * (float) Math.cos(angulo + 4.189);
        float y3 = y + tamanho * (float) Math.sin(angulo + 4.189);

        canvas.drawLine(x1, y1, x2, y2, paintCanhao);
        canvas.drawLine(x2, y2, x3, y3, paintCanhao);
        canvas.drawLine(x3, y3, x1, y1, paintCanhao);
        canvas.drawCircle(x, y, 15, paintCanhao);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (jogo == null) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                if (!jogo.getCanhoes().isEmpty()) {
                    Canhao canhao = jogo.getCanhoes().get(0);
                    double dx = event.getX() - canhao.getX();
                    double dy = event.getY() - canhao.getY();
                    double angulo = Math.atan2(dy, dx);
                    canhao.mover(canhao.getX(), canhao.getY(), angulo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void parar() {
        podeDesenhar = false;
    }
}
