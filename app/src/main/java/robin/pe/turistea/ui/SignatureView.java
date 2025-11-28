package robin.pe.turistea.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class SignatureView extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean hasBeenSigned = false; // **LA SOLUCIÓN ESTÁ AQUÍ**

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(8f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            clear(); // Limpia al inicio para establecer el fondo y el estado
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                hasBeenSigned = true; // El usuario ha empezado a firmar
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (drawCanvas != null) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    /**
     * Limpia el canvas de la firma y resetea el estado.
     */
    public void clear() {
        if (drawCanvas != null) {
            drawCanvas.drawColor(Color.WHITE);
            invalidate();
            hasBeenSigned = false; // La firma ha sido borrada
        }
    }

    /**
     * Devuelve un Bitmap de la firma actual.
     * @return El bitmap de la firma, o null si está vacío.
     */
    public Bitmap getSignatureBitmap() {
        if (hasBeenSigned && canvasBitmap != null) {
            return canvasBitmap;
        } else {
            return null;
        }
    }
}
