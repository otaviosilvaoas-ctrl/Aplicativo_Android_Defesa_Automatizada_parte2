package com.example.autotarget;

import android.view.View;
import android.graphics.Paint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.util.List;
import java.util.Locale;

/**
 * View customizada que renderiza o jogo no Canvas com feedback visual melhorado e HUD AV2.
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
    private Paint paintRec;
    private Paint paintIA;
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
        paintHUD.setTextSize(35);

        paintBarraFundo = new Paint();
        paintBarraFundo.setColor(Color.DKGRAY);

        paintBarraEnergia = new Paint();
        paintBarraEnergia.setColor(Color.GREEN);

        paintLegenda = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLegenda.setTextSize(25);
        paintLegenda.setColor(Color.WHITE);

        paintDivisoria = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDivisoria.setColor(Color.WHITE);
        paintDivisoria.setAlpha(80);
        paintDivisoria.setStrokeWidth(2);

        paintRec = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRec.setTextSize(22);
        paintRec.setColor(Color.CYAN);

        paintIA = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintIA.setTextSize(24);
        paintIA.setColor(Color.YELLOW);

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

        // Cálculo de FPS
        long now = System.currentTimeMillis();
        frameCount++;
        if (now - lastTime >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            lastTime = now;
            GerenciadorMetricas.registrarFPS(currentFps);
        }

        canvas.drawColor(Color.BLACK);

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

        desenharHUD(canvas);

        invalidate();
    }

    private void desenharHUD(Canvas canvas) {
        float padding = 30;
        float larguraBarra = 200;
        float alturaBarra = 20;

        // Lado Esquerdo
        int energiaEsq = jogo.getEnergiaEsquerda();
        int qtdEsq = jogo.getQtdCanhoesLado(true);
        double penEsq = jogo.getPenalidadeLado(true);
        
        paintHUD.setTextAlign(Paint.Align.LEFT);
        paintHUD.setColor(Color.WHITE);
        canvas.drawText("ESQUERDA: " + jogo.getAbatesEsquerda() + " Abates", padding, 60, paintHUD);
        canvas.drawText("Canhões: " + qtdEsq, padding, 100, paintHUD);
        
        canvas.drawRect(padding, 115, padding + larguraBarra, 115 + alturaBarra, paintBarraFundo);
        paintBarraEnergia.setColor(energiaEsq > 30 ? Color.GREEN : Color.RED);
        canvas.drawRect(padding, 115, padding + (larguraBarra * energiaEsq / 100f), 115 + alturaBarra, paintBarraEnergia);
        canvas.drawText("Energia: " + energiaEsq + "%", padding, 165, paintHUD);
        
        // IA AV2 (Esquerda)
        paintIA.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format(Locale.US, "Utilidade: %.2f | Decisão: %s", 
                jogo.getUtilidadeEsq(), jogo.getÚltimaDecisaoEsq()), padding, 205, paintIA);

        if (penEsq > 0) {
            paintHUD.setColor(Color.RED);
            canvas.drawText("Penalidade: +" + (int)(penEsq * 100) + "% delay", padding, 245, paintHUD);
        }

        // Lado Direito
        int energiaDir = jogo.getEnergiaDireita();
        int qtdDir = jogo.getQtdCanhoesLado(false);
        double penDir = jogo.getPenalidadeLado(false);
        float xDir = getWidth() - padding;

        paintHUD.setTextAlign(Paint.Align.RIGHT);
        paintHUD.setColor(Color.WHITE);
        canvas.drawText("DIREITA: " + jogo.getAbatesDireita() + " Abates", xDir, 60, paintHUD);
        canvas.drawText("Canhões: " + qtdDir, xDir, 100, paintHUD);

        canvas.drawRect(xDir - larguraBarra, 115, xDir, 115 + alturaBarra, paintBarraFundo);
        paintBarraEnergia.setColor(energiaDir > 30 ? Color.GREEN : Color.RED);
        canvas.drawRect(xDir - (larguraBarra * energiaDir / 100f), 115, xDir, 115 + alturaBarra, paintBarraEnergia);
        canvas.drawText("Energia: " + energiaDir + "%", xDir, 165, paintHUD);

        // IA AV2 (Direita)
        paintIA.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.US, "Utilidade: %.2f | Decisão: %s", 
                jogo.getUtilidadeDir(), jogo.getÚltimaDecisaoDir()), xDir, 205, paintIA);

        if (penDir > 0) {
            paintHUD.setColor(Color.RED);
            canvas.drawText("Penalidade: +" + (int)(penDir * 100) + "% delay", xDir, 245, paintHUD);
        }

        // HUD AV2: Informações de Reconciliação (Centro Inferior)
        paintRec.setTextAlign(Paint.Align.CENTER);
        String infoRec = String.format(Locale.US, "Rec. Ativa | Erro: %.2f -> %.2f | Leituras: %d", 
                jogo.getErroRecAntes(), jogo.getErroRecDepois(), jogo.getLeiturasRecUsadas());
        canvas.drawText(infoRec, getWidth() / 2f, getHeight() - 50, paintRec);

        // Centro/Logs
        paintHUD.setTextAlign(Paint.Align.CENTER);
        paintHUD.setColor(Color.GRAY);
        paintHUD.setTextSize(25);
        List<String> logs = jogo.getLogsTela();
        for (int i = 0; i < logs.size(); i++) {
            canvas.drawText(logs.get(i), getWidth()/2f, getHeight() - 150 - (i * 35), paintHUD);
        }

        if (GerenciadorMetricas.DEBUG) {
            paintHUD.setColor(Color.YELLOW);
            canvas.drawText("FPS: " + currentFps, getWidth()/2f, 40, paintHUD);
        }
    }

    private void drawCanhaoComFeedback(Canvas canvas, Canhao canhao) {
        float x = (float) canhao.getX();
        float y = (float) canhao.getY();
        float ang = (float) canhao.getAngulo();
        float tam = 45;

        float x1 = x + tam * (float) Math.cos(ang);
        float y1 = y + tam * (float) Math.sin(ang);
        float x2 = x + tam * (float) Math.cos(ang + 2.094);
        float y2 = y + tam * (float) Math.sin(ang + 2.094);
        float x3 = x + tam * (float) Math.cos(ang + 4.189);
        float y3 = y + tam * (float) Math.sin(ang + 4.189);

        paintCanhao.setColor(x < getWidth()/2f ? Color.GREEN : Color.CYAN);
        canvas.drawLine(x1, y1, x2, y2, paintCanhao);
        canvas.drawLine(x2, y2, x3, y3, paintCanhao);
        canvas.drawLine(x3, y3, x1, y1, paintCanhao);
        canvas.drawCircle(x, y, 12, paintCanhao);

        paintTexto.setColor(Color.WHITE);
        paintTexto.setTextSize(25);
        canvas.drawText("C" + canhao.getId(), x, y - 55, paintTexto);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (jogo == null) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            Canhao selecionado = null;
            double menorDist = Double.MAX_VALUE;
            for (Canhao c : jogo.getCanhoes()) {
                double d = Math.hypot(event.getX() - c.getX(), event.getY() - c.getY());
                if (d < 250 && d < menorDist) {
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
