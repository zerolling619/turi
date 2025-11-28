package robin.pe.turistea.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.math.max

data class RoutePoint(
    val id: Int,
    val index: Int,
    val title: String,
    val description: String,
    val bg_image: String
)

class RoutePathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val path = Path()
    private val paintPath = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#053755")
    }

    private val endpointStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }

    private val pendingPathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#7A7D8B") // gris azulado
    }

    var points: List<RoutePoint> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    // Anclas de inicio/fin en porcentaje del tamaño del View (0f..1f)
    var startXPercent: Float = 0.15f
    var startYPercent: Float = 0.80f
    var endXPercent: Float = 0.85f
    var endYPercent: Float = 0.30f

    fun setAnchors(startX: Float, startY: Float, endX: Float, endY: Float) {
        startXPercent = startX.coerceIn(0f, 1f)
        startYPercent = startY.coerceIn(0f, 1f)
        endXPercent = endX.coerceIn(0f, 1f)
        endYPercent = endY.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isEmpty()) return

        // Crear gradiente
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            Color.parseColor("#A855F7"), // morado
            Color.parseColor("#7DD3FC"), // celeste (similar a imagen 2)
            Shader.TileMode.CLAMP
        )
        paintPath.shader = gradient

        // Dibujar PATH como curvas
        path.reset()

        val spacingX = width / (points.size + 1f)
        var currentX = width * startXPercent
        var currentY = height * startYPercent

        path.moveTo(currentX, currentY)

        if (points.size == 1) {
            // Si hay un solo punto, conectar de ancla inicio a ancla fin
            val endX = width * endXPercent
            val endY = height * endYPercent
            path.quadTo(
                (currentX + endX) / 2,
                (currentY + endY) / 2,
                endX,
                endY
            )
            currentX = endX
            currentY = endY
        } else {
            // Construir posiciones de nodos utilizando la misma función que los puntos
            val nodePositions = mutableListOf<Pair<Float, Float>>()
            // inicio
            nodePositions.add(width * startXPercent to height * startYPercent)
            val count = points.size
            if (count == 2) {
                // añadir punto medio para tener 3 nodos visibles (inicio, medio, fin)
                val midT = 0.5f
                val midX = width * (startXPercent + (endXPercent - startXPercent) * midT)
                val midWave = sin(midT * Math.PI).toFloat()
                val midYBase = startYPercent + (endYPercent - startYPercent) * midT
                val midY = height * midYBase - midWave * height * 0.08f
                nodePositions.add(midX to midY)
            } else {
                // puntos intermedios
                for (i in 1 until count - 1) {
                    val t = i.toFloat() / (count - 1).toFloat()
                    val x = width * (startXPercent + (endXPercent - startXPercent) * t)
                    val wave = sin(t * Math.PI).toFloat()
                    val yBase = startYPercent + (endYPercent - startYPercent) * t
                    val y = height * yBase - wave * height * 0.08f
                    nodePositions.add(x to y)
                }
            }
            // fin
            nodePositions.add(width * endXPercent to height * endYPercent)

            // Dibujar path alineado exactamente a los nodos
            currentX = nodePositions.first().first
            currentY = nodePositions.first().second
            path.reset()
            path.moveTo(currentX, currentY)
            for (i in 1 until nodePositions.size) {
                val nx = nodePositions[i].first
                val ny = nodePositions[i].second
                path.quadTo(
                    (currentX + nx) / 2f,
                    (currentY + ny) / 2f,
                    nx,
                    ny
                )
                currentX = nx
                currentY = ny
            }
        }

        canvas.drawPath(path, paintPath)

        // Dibujar segmento "pendiente" en gris (último 25% del path)
        if (points.size >= 3) {
            val splitIndex = (points.size * 0.75f).toInt().coerceAtMost(points.size - 1)
            val pendingPath = Path()
            var px = width * (startXPercent + (endXPercent - startXPercent) * (splitIndex / points.size.toFloat()))
            var py = height * (startYPercent + (endYPercent - startYPercent) * (splitIndex / points.size.toFloat()))
            pendingPath.moveTo(px, py)
            for (i in splitIndex until points.size - 1) {
                val nextX = width * (startXPercent + (endXPercent - startXPercent) * ((i + 1) / points.size.toFloat()))
                val t = (i + 1f) / points.size
                val wave = sin(t * Math.PI).toFloat()
                val yInterp = startYPercent + (endYPercent - startYPercent) * t
                val nextY = height * yInterp - wave * height * 0.08f
                pendingPath.quadTo((px + nextX) / 2, (py + nextY) / 2, nextX, nextY)
                px = nextX
                py = nextY
            }
            canvas.drawPath(pendingPath, pendingPathPaint)
        }

        // Dibujar círculos de puntos y extremos
        // Recalcular posiciones siguiendo la misma función determinística
        fun pointPos(i: Int): Pair<Float, Float> {
            val denom = max((points.size - 1).toFloat(), 1f)
            val t = (i.toFloat()) / denom
            val x = width * (startXPercent + (endXPercent - startXPercent) * t)
            val wave = sin(t * Math.PI).toFloat()
            val yBase = startYPercent + (endYPercent - startYPercent) * t
            val y = height * yBase - wave * height * 0.08f
            return x to y
        }

        if (points.size == 1) {
            // Dos endpoints para un único punto
            val startX = width * startXPercent
            val startY = height * startYPercent
            val endX = width * endXPercent
            val endY = height * endYPercent
            canvas.drawCircle(startX, startY, 12f, pointPaint)
            canvas.drawCircle(startX, startY, 16f, endpointStrokePaint)
            canvas.drawCircle(endX, endY, 12f, pointPaint)
            canvas.drawCircle(endX, endY, 16f, endpointStrokePaint)
        } else {
            // Construir nodos para círculos, consistente con el path
            val circleNodes = mutableListOf<Pair<Float, Float>>()
            circleNodes.add(width * startXPercent to height * startYPercent)
            if (points.size == 2) {
                val midT = 0.5f
                val mx = width * (startXPercent + (endXPercent - startXPercent) * midT)
                val mw = sin(midT * Math.PI).toFloat()
                val myb = startYPercent + (endYPercent - startYPercent) * midT
                val my = height * myb - mw * height * 0.08f
                circleNodes.add(mx to my)
            } else {
                for (i in 1 until points.size - 1) {
                    val t = i.toFloat() / (points.size - 1).toFloat()
                    val x = width * (startXPercent + (endXPercent - startXPercent) * t)
                    val w = sin(t * Math.PI).toFloat()
                    val yb = startYPercent + (endYPercent - startYPercent) * t
                    val y = height * yb - w * height * 0.08f
                    circleNodes.add(x to y)
                }
            }
            circleNodes.add(width * endXPercent to height * endYPercent)

            // Dibujar círculos
            for (i in circleNodes.indices) {
                val (x, y) = circleNodes[i]
                val radius = if (i == 0 || i == circleNodes.lastIndex) 12f else 10f
                canvas.drawCircle(x, y, radius, pointPaint)
                if (i == 0 || i == circleNodes.lastIndex) {
                    canvas.drawCircle(x, y, 16f, endpointStrokePaint)
                }
            }
        }
    }
}
