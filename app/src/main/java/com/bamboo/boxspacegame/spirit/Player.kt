package com.bamboo.boxspacegame.spirit

import android.graphics.*
import android.util.SizeF
import androidx.core.graphics.withRotation
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Player : BaseSprite() {
    private var lockAngle = 0f // 移动时锁定的角度
    private var isMove = false
    private var isLockAngle = false
    private var isAttack = false
    private val paint = Paint()
    var size = SizeF(0f, 0f) // 玩家的尺寸
    var isShow = true

    fun init(scope: CoroutineScope) {
        this.distance = 3f // 玩家的移动距离
        // 设置精灵的尺寸为SurfaceView的1/20
        this.size = SizeF(AppGobal.screenWidth / 20f, AppGobal.screenWidth / 20f)
        this.angle = 270f
        // 初始位置为屏幕居中
        this.x = (AppGobal.screenWidth - size.width) / 2
        this.y = (AppGobal.screenHeight - size.height) / 2
        buildBmp() // 绘制玩家的图形
        scope.launch(Dispatchers.IO) {
            while (true) {
                if (AppGobal.pause) continue
                if (isMove && isShow) {
                    move()
                }
                delay(5)
            }
        }
        scope.launch(Dispatchers.IO) {
            while (true) {
                if (AppGobal.pause) continue
                if (isAttack) {
                    BulletManager
                        .obtain()
                        .send(size.width / 2 + x, size.height / 2 + y, lockAngle)
                }
                delay(Bullet.INTERVAL) // 发射子弹的间隔
            }
        }
    }

    /**
     * 绘制玩家精灵的BITMAP并缓存
     */
    private fun buildBmp() {
        val bmp = Bitmap.createBitmap(
            size.width.toInt(),
            size.height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        Canvas(bmp).apply {
            val paint = Paint()
            paint.color = Color.WHITE
            paint.shader = RadialGradient(
                size.width / 2f, 0f, AppGobal.unitSize,
                intArrayOf(Color.WHITE, Color.DKGRAY), null,
                Shader.TileMode.CLAMP
            )
            val path = Path()
            path.moveTo(size.width / 2f, 0f)
            path.lineTo(size.width, size.height - (size.height / 3))
            path.lineTo(size.width / 2f, size.height)
            path.lineTo(0f, size.height - (size.height / 3))
            path.close()
            this.drawPath(path, paint)
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.shader = null
            paint.strokeWidth = 1f
            paint.strokeJoin = Paint.Join.ROUND
            this.drawPath(path, paint)
            this.drawLine(size.width / 2, 0f, size.width / 2, size.height, paint)
        }
        AppGobal.bmpCache.put(AppGobal.BMP_PLAYER, bmp)
    }

    override fun draw(canvas: Canvas) {
        val bmp = AppGobal.bmpCache[AppGobal.BMP_PLAYER]
        bmp?.let {
            canvas.withRotation(
                (if (!isLockAngle) angle else lockAngle) + 90,
                x + size.width / 2,
                y + size.height / 2
            ) {
                if (isShow) {
                    if (isMove) {
                        drawMove(this)
                    }
                    this.drawBitmap(it, x, y, null)
                }
            }
        }
    }

    /**
     * 绘制飞行器的尾焰
     */
    private fun drawMove(canvas: Canvas) {
        val cz = size.width / 5
        val cz2 = System.currentTimeMillis() % 12
        val cx = x + size.width / 2
        val cy = y + size.height - cz / 2
        paint.style = Paint.Style.FILL
        paint.maskFilter = BlurMaskFilter(cz * 2 + cz2, BlurMaskFilter.Blur.SOLID)
        paint.shader = RadialGradient(
            cx, cy, cz + cz2,
            intArrayOf(Color.WHITE, Color.parseColor("#91C8EB")), null,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, cz + cz2, paint)
    }

    override fun move() {
        // 根据距离和角度计算移动后的坐标
        val pt = MathUtils.getCoordsByAngle(distance, angle.toDouble(), PointF(x, y))
        // 防止越界
        if (pt.x < AppGobal.unitSize) pt.x = AppGobal.unitSize
        if (pt.y < AppGobal.unitSize) pt.y = AppGobal.unitSize
        if (pt.x > AppGobal.screenWidth - AppGobal.unitSize - size.width)
            pt.x = AppGobal.screenWidth - AppGobal.unitSize - size.width
        if (pt.y > AppGobal.screenHeight - AppGobal.unitSize - size.height)
            pt.y = AppGobal.screenHeight - AppGobal.unitSize - size.height
        // 更新玩家坐标
        x = pt.x
        y = pt.y
    }

    fun actionRelease() {
        isMove = false
    }

    fun actionMove() {
        isMove = true
    }

    fun lockAngle() {
        isLockAngle = true
        lockAngle = angle
    }

    fun unlockAngle() {
        isLockAngle = false
        angle = lockAngle
    }

    fun sendBullet(isAttack: Boolean) {
        Player.isAttack = isAttack
    }

    fun jump() {
        if (AppGobal.pause) return
        isShow = false
        // 通过事件机制播放音效
        LiveEventBus.get(AppGobal.EVENT_FLASH_SFX).post(true)
        // 播放瞬移动画，在动画结束时移动玩家的坐标
        EffectManager.obtainFlash().play(x, y) {
            val bakDistance = distance
            distance = size.width * 2
            move()
            distance = bakDistance
            EffectManager.obtainFlash().play(x, y, true) {
                isShow = true
            }
        }
    }

    fun getRect() = RectF(x, y, x + AppGobal.unitSize, y + AppGobal.unitSize)
}