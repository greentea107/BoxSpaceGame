package com.bamboo.boxspacegame.spirit

import android.graphics.*
import android.util.SizeF
import androidx.core.graphics.withRotation
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
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
    var power = 0 // 能量值

    init {
        this.distance = 3f // 玩家的移动距离
        // 设置精灵的尺寸为SurfaceView的1/20
        this.size = SizeF(AppGobal.unitSize, AppGobal.unitSize)
        this.angle = 270f // 初始化玩家的角度为12点钟方向
        // 初始位置为屏幕居中
        this.x = (AppGobal.screenWidth - size.width) / 2
        this.y = (AppGobal.screenHeight - size.height) / 2
        buildBmp() // 绘制玩家的图形
    }

    fun initScope(scope: CoroutineScope) {
        this.power = AppGobal.POWER_MAX / 2 // 能量的初始值
        // 由于玩家的移动是连续的，所以需要通过循环来实现
        scope.launch(Dispatchers.Default) {
            while (AppGobal.isRunning) {
                if (AppGobal.pause) continue
                if (isMove && isShow) {
                    move()
                }
                delay(5)
            }
        }
        // 由于子弹是连续发射的，所以需要一个循环来处理，每次循环时根据isAttack判断是否需要发射
        scope.launch(Dispatchers.Default) {
            while (AppGobal.isRunning) {
                if (AppGobal.pause) continue
                if (isAttack && isShow) {
                    BulletManager.sendTargetEnemy(
                        size.width / 2 + x,
                        size.height / 2 + y,
                        lockAngle
                    )
                    // 子弹充能
                    if (power < AppGobal.POWER_MAX) power++
                }
                delay(Bullet.INTERVAL) // 发射子弹的间隔
            }
        }
    }

    /**
     * 将玩家的图像绘制到Bitmap上并缓存
     */
    private fun buildBmp() {
        val bmp = Bitmap.createBitmap(
            size.width.toInt(),
            size.height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        // 手绘玩家的图像
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
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.shader = null
            paint.color = Color.WHITE
            paint.strokeJoin = Paint.Join.ROUND
            this.drawPath(path, paint)
            this.drawLine(size.width / 2, 0f, size.width / 2, size.height, paint)
        }
        AppGobal.bmpCache.put(AppGobal.BMP_PLAYER, bmp)
    }

    /**
     * 从缓存中取出玩家的Bitmap并绘制在屏幕上
     */
    override fun draw(canvas: Canvas) {
        val bmp = AppGobal.bmpCache[AppGobal.BMP_PLAYER]
        if (bmp == null || bmp.width == 0) buildBmp()
        bmp?.let {
            canvas.withRotation(
                (if (!isLockAngle) angle else lockAngle) + 90,
                x + size.width / 2,
                y + size.height / 2
            ) {
                if (isShow) {
                    if (isMove) {
                        drawMotion(this)
                    }
                    this.drawBitmap(it, x, y, null)
                }
            }
        }
    }

    /**
     * 绘制飞行器的尾焰
     */
    private fun drawMotion(canvas: Canvas) {
        val cz = size.width / 5f
        val cx = x + size.width / 2f
        val cy = y + size.height - cz / 2f
        val shake = System.currentTimeMillis() % 12
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, cz + shake,
            intArrayOf(
                Color.WHITE,
                Color.argb(180, 120, 180, 224),
            ), null,
            Shader.TileMode.CLAMP
        )
        canvas.drawOval(
            cx - shake - size.width / 2,
            cy - shake - cz / 2,
            cx + shake + size.width / 2,
            cy + shake + cz / 2,
            paint
        )
        canvas.drawCircle(cx, cy, cz + shake, paint)
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

    /**
     * 发射子弹
     */
    fun sendBullet(isAttack: Boolean) {
        if (AppGobal.pause) return
        Player.isAttack = isAttack
    }

    /**
     * 瞬移
     * 步骤1：隐藏玩家图像
     * 步骤2：播放瞬移动画并在结束后计算玩家瞬移后的坐标
     * 步骤3：再次播放动画并在结束后显示玩家
     */
    fun jump() {
        if (AppGobal.pause) return
        if (power < 50) return
        isShow = false
        power -= 50
        // 播放音效
        LiveEventBus.get(AppGobal.EVENT_FLASH_SFX).post(true)
        // 播放瞬移动画，在动画结束时移动玩家的坐标
        EffectManager.obtainFlash().play(x, y) {
            val bakDistance = distance
            distance = size.width * 3
            move()
            distance = bakDistance
            EffectManager.obtainFlash().play(x, y, true) {
                isShow = true
            }
        }
    }

    /**
     * 发射爆雷
     */
    fun sendBomb() {
        if (AppGobal.pause) return
        // 如果能量低于最大值则不能使用
        if (power < AppGobal.POWER_MAX) return
        power = 0 // 使用完毕后能量清空
        // 隐藏玩家后播放闪避动画，然后播放爆雷动画并清敌，最后再显示玩家
        isShow = false
        EffectManager.obtainFlash().play(x, y, false) {
            EffectManager.obtainGrenade().play(
                x + (size.width / 2),
                y + (size.height / 2)
            ) {
                EffectManager.obtainFlash().play(x, y) {
                    isShow = true
                }
            }
            StageManager.clearAllEnemy() // 清敌
        }
    }

    /**
     * 获取玩家在屏幕上的矩形
     */
    fun getRect() = RectF(x, y, x + size.width, y + size.height)

    /**
     * 玩家被打击
     */
    fun beHit(bullet: Bullet) {
        power -= bullet.damage.toInt()
        if (power <= 0) {
            shotDown()
        }
    }

    /**
     * 玩家被击落
     */
    fun shotDown() {
        Player.isShow = false
        EffectManager.obtainBomb().play(Player.x + size.width, Player.y + size.height) {
            StageManager.gameStatus = StageManager.STATE_MISSION_FAILED
        }
    }
}