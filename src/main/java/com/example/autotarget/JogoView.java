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
    private boolean podeDesenhar;

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
        paintTexto.setTextSize(60); // Aumentado para melhor visibilidade
        paintTexto.setStyle(Paint.Style.FILL);

        podeDesenhar = true;
    }

    /**
     * Define a instância do jogo a ser renderizada.
     */
    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (jogo == null || !podeDesenhar) {
            return;
        }

        // Desenha fundo
        canvas.drawColor(Color.BLACK);

        // Desenha linha divisória
        Paint linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha(50); // Transparente para não atrapalhar
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

        // O invalidate() aqui faz a animação ser contínua
        invalidate();
    }

    /**
     * Desenha um canhão como um triângulo.
     */
    private void drawCanhao(Canvas canvas, Canhao canhao) {
        float x = (float) canhao.getX();
        float y = (float) canhao.getY();
        float angulo = (float) canhao.getAngulo();
        float tamanho = 50; // Aumentado um pouco

        // Calcula os vértices do triângulo baseado no ângulo
        float x1 = x + tamanho * (float) Math.cos(angulo);
        float y1 = y + tamanho * (float) Math.sin(angulo);

        float x2 = x + tamanho * (float) Math.cos(angulo + 2.094); // 120 graus
        float y2 = y + tamanho * (float) Math.sin(angulo + 2.094);

        float x3 = x + tamanho * (float) Math.cos(angulo + 4.189); // 240 graus
        float y3 = y + tamanho * (float) Math.sin(angulo + 4.189);

        // Desenha o triângulo
        canvas.drawLine(x1, y1, x2, y2, paintCanhao);
        canvas.drawLine(x2, y2, x3, y3, paintCanhao);
        canvas.drawLine(x3, y3, x1, y1, paintCanhao);

        // Desenha círculo no centro
        canvas.drawCircle(x, y, 15, paintCanhao);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (jogo == null) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                // Mira o primeiro canhão para onde o usuário tocou/arrastou
                if (!jogo.getCanhoes().isEmpty()) {
                    Canhao canhao = jogo.getCanhoes().get(0);
                    double dx = x - canhao.getX();
                    double dy = y - canhao.getY();
                    double angulo = Math.atan2(dy, dx);
                    canhao.mover(canhao.getX(), canhao.getY(), angulo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Para o desenho.
     */
    public void parar() {
        podeDesenhar = false;
    }
}