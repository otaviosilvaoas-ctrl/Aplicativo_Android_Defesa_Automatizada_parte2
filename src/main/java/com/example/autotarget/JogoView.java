package com.example.autotarget;

import android.view.View;
import android.graphics.Paint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.util.List;

/**
 * View customizada que renderiza o jogo no Canvas com feedback visual melhorado.
 */
public class JogoView extends View {

    private Jogo jogo;
    private Paint paintAlvo;
    private Paint paintCanhao;
    private Paint paintProjetil;
    private Paint paintTexto;
    private Paint paintHUD;
    private Paint paintBarraFundo;
    private Paint paintBarraEnergia;
    private Paint paintLegenda;
    private Paint paintDivisoria;
    private boolean podeDesenhar;

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

    private void init() {
        paintAlvo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAlvo.setStyle(Paint.Style.FILL);

        paintCanhao = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCanhao.setColor(Color.GREEN);
        paintCanhao.setStyle(Paint.Style.STROKE);
        paintCanhao.setStrokeWidth(5);

        paintProjetil = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintProjetil.setColor(Color.RED);
        paintProjetil.setStyle(Paint.Style.FILL);

        paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTexto.setColor(Color.WHITE);
        paintTexto.setTextSize(50);
        paintTexto.setTextAlign(Paint.Align.CENTER);

        paintHUD = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHUD.setColor(Color.WHITE);
        paintHUD.setTextSize(40);

        paintBarraFundo = new Paint();
        paintBarraFundo.setColor(Color.DKGRAY);

        paintBarraEnergia = new Paint();
        paintBarraEnergia.setColor(Color.GREEN);

        paintLegenda = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLegenda.setTextSize(30);
        paintLegenda.setColor(Color.WHITE);

        paintDivisoria = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDivisoria.setColor(Color.WHITE);
        paintDivisoria.setAlpha(120);
        paintDivisoria.setStrokeWidth(4);
        paintDivisoria.setStyle(Paint.Style.STROKE);

        podeDesenhar = true;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (jogo != null) jogo.setDimensoes(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (jogo == null || !podeDesenhar) return;

        long now = System.currentTimeMillis();
        frameCount++;
        if (now - lastTime >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            lastTime = now;
            GerenciadorMetricas.registrarFPS(currentFps);
        }

        canvas.drawColor(Color.BLACK);

        // Desenha Linha de Divisão Central
        float centroX = getWidth() / 2f;
        canvas.drawLine(centroX, 0, centroX, getHeight(), paintDivisoria);

        // Desenha alvos
        for (Alvo alvo : jogo.getAlvos()) {
            float x = (float) alvo.getX();
            float y = (float) alvo.getY();
            float r = (float) alvo.getRaio();

            if (alvo instanceof AlvoRapido) {
                paintAlvo.setColor(Color.YELLOW);
                canvas.drawCircle(x, y, r, paintAlvo);
                paintTexto.setColor(Color.BLACK);
                paintTexto.setTextSize(20);
                canvas.drawText("R", x, y + 7, paintTexto);
            } else {
                paintAlvo.setColor(Color.BLUE);
                canvas.drawCircle(x, y, r, paintAlvo);
                paintTexto.setColor(Color.WHITE);
                paintTexto.setTextSize(20);
                canvas.drawText("L", x, y + 7, paintTexto);
            }
        }

        // Desenha canhões
        for (Canhao canhao : jogo.getCanhoes()) {
            drawCanhaoComFeedback(canvas, canhao);
        }

        // Desenha projéteis
        for (Canhao canhao : jogo.getCanhoes()) {
            for (Projetil p : canhao.getProjeteis()) {
                canvas.drawCircle((float) p.getX(), (float) p.getY(), (float) p.getRaio(), paintProjetil);
            }
        }

        // HUD - Placar e Logs
        paintHUD.setColor(Color.WHITE);
        paintHUD.setTextSize(50);
        canvas.drawText("ABATES: " + jogo.getAbatesTotal(), 50, 80, paintHUD);

        desenharLegenda(canvas);

        // Logs de tela
        paintHUD.setTextSize(35);
        paintHUD.setColor(Color.LTGRAY);
        List<String> logs = jogo.getLogsTela();
        for (int i = 0; i < logs.size(); i++) {
            canvas.drawText("> " + logs.get(i), 50, 150 + (i * 45), paintHUD);
        }

        if (GerenciadorMetricas.DEBUG) {
            paintHUD.setColor(Color.YELLOW);
            canvas.drawText("FPS: " + currentFps, 50, getHeight() - 50, paintHUD);
        }

        invalidate();
    }

    private void desenharLegenda(Canvas canvas) {
        float xBase = getWidth() - 220;
        float yBase = 60;
        float raioLegenda = 15;

        paintAlvo.setColor(Color.YELLOW);
        canvas.drawCircle(xBase, yBase, raioLegenda, paintAlvo);
        paintLegenda.setColor(Color.WHITE);
        canvas.drawText("Rápido", xBase + 30, yBase + 10, paintLegenda);

        paintAlvo.setColor(Color.BLUE);
        canvas.drawCircle(xBase, yBase + 50, raioLegenda, paintAlvo);
        paintLegenda.setColor(Color.WHITE);
        canvas.drawText("Lento", xBase + 30, yBase + 60, paintLegenda);
    }

    private void drawCanhaoComFeedback(Canvas canvas, Canhao canhao) {
        float x = (float) canhao.getX();
        float y = (float) canhao.getY();
        float ang = (float) canhao.getAngulo();
        float tam = 50;

        float x1 = x + tam * (float) Math.cos(ang);
        float y1 = y + tam * (float) Math.sin(ang);
        float x2 = x + tam * (float) Math.cos(ang + 2.094);
        float y2 = y + tam * (float) Math.sin(ang + 2.094);
        float x3 = x + tam * (float) Math.cos(ang + 4.189);
        float y3 = y + tam * (float) Math.sin(ang + 4.189);

        canvas.drawLine(x1, y1, x2, y2, paintCanhao);
        canvas.drawLine(x2, y2, x3, y3, paintCanhao);
        canvas.drawLine(x3, y3, x1, y1, paintCanhao);
        canvas.drawCircle(x, y, 15, paintCanhao);

        paintTexto.setColor(Color.GREEN);
        paintTexto.setTextSize(30);
        paintTexto.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("C" + canhao.getId(), x, y - 65, paintTexto);

        float larguraBarra = 80;
        float alturaBarra = 10;
        float porcentagem = (float) canhao.getEnergia() / canhao.getEnergiaMaxima();
        
        canvas.drawRect(x - larguraBarra/2, y + 60, x + larguraBarra/2, y + 60 + alturaBarra, paintBarraFundo);
        paintBarraEnergia.setColor(porcentagem > 0.3f ? Color.GREEN : Color.RED);
        canvas.drawRect(x - larguraBarra/2, y + 60, x - larguraBarra/2 + (larguraBarra * porcentagem), y + 60 + alturaBarra, paintBarraEnergia);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (jogo == null) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            Canhao selecionado = null;
            double menorDist = Double.MAX_VALUE;
            for (Canhao c : jogo.getCanhoes()) {
                double d = Math.hypot(event.getX() - c.getX(), event.getY() - c.getY());
                if (d < 300 && d < menorDist) {
                    menorDist = d;
                    selecionado = c;
                }
            }
            if (selecionado != null) {
                double dx = event.getX() - selecionado.getX();
                double dy = event.getY() - selecionado.getY();
                selecionado.mover(selecionado.getX(), selecionado.getY(), Math.atan2(dy, dx));
            }
        }
        return true;
    }

    public void parar() {
        podeDesenhar = false;
    }
}
