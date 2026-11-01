package com.sync.app.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

/** Kyant `DialogContent` glass: colorControls + blur 8dp + lens(24dp, 48dp, depthEffect) + plain highlight. */
private fun Modifier.dialogGlass(backdrop: Backdrop, radius: Int, popIn: Float, lensH: Float, lensA: Float): Modifier =
    this.drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedRectangle(radius.dp) },
        effects = {
            colorControls(brightness = 0f, saturation = 1.5f)
            blur(3f.dp.toPx())
            lens(lensH.dp.toPx(), lensA.dp.toPx(), depthEffect = true)
        },
        highlight = { Highlight.Plain },
        layerBlock = {
            val s = popScale(popIn)
            scaleX = s; scaleY = s; alpha = popIn.coerceIn(0f, 1f)
        },
        onDrawSurface = { drawRect(GlassTheme.Container) }
    )

private val NoTouchThrough: Modifier = Modifier.pointerInput(Unit) {
    // consume taps on the glass body so they don't reach the DOM overlay behind
    detectTapGestures { }
}

@Composable
private fun PillButton(label: String, fill: Color, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(Capsule())
            .background(fill)
            .tap(onClick = onClick)
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) { BasicText(label, style = TextStyle(textColor, 16.sp, FontWeight.Medium), maxLines = 1) }
}

@Composable
private fun InputCapsule(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(Capsule())
            .background(Color.White.copy(0.10f))
            .height(48.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) { content() }
}

@Composable
internal fun DialogGlass(d: DialogState, backdrop: Backdrop, c: GlassController) {
    val pop = rememberPopIn()
    Box(Modifier.cssRect(d.rect), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true)
                .dialogGlass(backdrop, 48, pop.value, 24f, 48f)
                .then(NoTouchThrough)
        ) {
            BasicText(
                d.title, Modifier.padding(28.dp, 24.dp, 28.dp, 12.dp),
                style = TextStyle(GlassTheme.Content, 22.sp, FontWeight.Medium)
            )
            InputCapsule(Modifier.padding(24.dp, 6.dp, 24.dp, 12.dp).fillMaxWidth()) {
                MirroredField(
                    "pl-dialog-input", d.value, d.focused, d.placeholder, c,
                    ImeAction.Done, {}, 16.sp, Modifier.fillMaxWidth()
                )
            }
            Row(
                Modifier.padding(24.dp, 12.dp, 24.dp, 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillButton(d.cancelLabel, Color.White.copy(0.12f), GlassTheme.Content, Modifier.weight(1f)) {
                    c.cmd { put("t", "dlg"); put("a", "cancel") }
                }
                PillButton(d.okLabel, LocalGlassAccent.current, Color.White, Modifier.weight(1f)) {
                    c.cmd { put("t", "dlg"); put("a", "ok") }
                }
            }
        }
    }
}

@Composable
internal fun ConfirmGlass(d: ConfirmState, backdrop: Backdrop, c: GlassController) {
    val pop = rememberPopIn()
    Box(Modifier.cssRect(d.rect), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true)
                .dialogGlass(backdrop, 48, pop.value, 24f, 48f)
                .then(NoTouchThrough)
        ) {
            BasicText(
                d.title, Modifier.padding(28.dp, 24.dp, 28.dp, 8.dp),
                style = TextStyle(GlassTheme.Content, 22.sp, FontWeight.Medium)
            )
            if (d.message.isNotBlank()) {
                BasicText(
                    d.message,
                    Modifier
                        .graphicsLayer(blendMode = BlendMode.Plus)
                        .padding(28.dp, 4.dp, 28.dp, 8.dp),
                    style = TextStyle(GlassTheme.Content.copy(0.68f), 15.sp), maxLines = 5
                )
            }
            Row(
                Modifier.padding(24.dp, 16.dp, 24.dp, 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillButton(d.cancelLabel, Color.White.copy(0.12f), GlassTheme.Content, Modifier.weight(1f)) {
                    c.cmd { put("t", "cfm"); put("a", "cancel") }
                }
                PillButton(d.okLabel, GlassTheme.Danger, Color.White, Modifier.weight(1f)) {
                    c.cmd { put("t", "cfm"); put("a", "ok") }
                }
            }
        }
    }
}

@Composable
internal fun ContextMenuGlass(m: CtxState, backdrop: Backdrop, c: GlassController) {
    val pop = rememberPopIn()
    Box(Modifier.cssRect(m.rect.copy(h = 10f))) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .dialogGlass(backdrop, 28, pop.value, 16f, 32f)
                .then(NoTouchThrough)
                .padding(vertical = 10.dp)
        ) {
            BasicText(
                m.title, Modifier.padding(20.dp, 8.dp, 20.dp, 6.dp),
                style = TextStyle(GlassTheme.Content.copy(0.6f), 12.sp, FontWeight.Medium)
            )
            m.items.forEachIndexed { i, name ->
                MenuRow(name, GlassTheme.Content) { c.cmd { put("t", "ctx"); put("i", i) } }
            }
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth().height(0.5.dp)
                .background(Color.White.copy(0.18f)))
            MenuRow(m.newLabel, LocalGlassAccent.current) { c.cmd { put("t", "ctx"); put("new", true) } }
        }
    }
}

@Composable
private fun MenuRow(label: String, color: Color, onClick: () -> Unit) {
    BasicText(
        label,
        Modifier.fillMaxWidth().tap(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        style = TextStyle(color, 15.sp), maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun AddModalGlass(a: AddModalState, backdrop: Backdrop, c: GlassController) {
    val pop = rememberPopIn()
    Box(Modifier.cssRect(a.rect), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxSize()
                .dialogGlass(backdrop, 40, pop.value, 24f, 44f)
                .then(NoTouchThrough)
        ) {
            Row(
                Modifier.padding(24.dp, 20.dp, 16.dp, 8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicText(
                    a.title, Modifier.weight(1f),
                    style = TextStyle(GlassTheme.Content, 22.sp, FontWeight.Medium)
                )
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(0.12f))
                        .tap { c.cmd { put("t", "add"); put("a", "close") } },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(12.dp)) {
                        strokeIcon(GlassIcons.Close, 12f, GlassTheme.Content, 1.8f)
                    }
                }
            }
            InputCapsule(Modifier.padding(20.dp, 4.dp, 20.dp, 10.dp).fillMaxWidth()) {
                MirroredField(
                    "pl-add-modal-inp", a.value, a.focused, a.placeholder, c,
                    ImeAction.Search, {}, 15.sp, Modifier.fillMaxWidth()
                )
            }
            if (a.rows.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BasicText(
                        a.hint ?: "",
                        style = TextStyle(GlassTheme.Content.copy(0.6f), 14.sp, textAlign = TextAlign.Center)
                    )
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 14.dp)) {
                    itemsIndexed(a.rows) { i, r -> AddRowItem(i, r, c) }
                }
            }
        }
    }
}

@Composable
private fun AddRowItem(index: Int, r: AddRow, c: GlassController) {
    val art = rememberRemoteImage(r.art)
    val accent = LocalGlassAccent.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.08f))) {
            if (art != null) Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(r.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = TextStyle(GlassTheme.Content, 14.sp, FontWeight.Medium))
            BasicText(r.channel, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = TextStyle(GlassTheme.Content.copy(0.6f), 12.sp))
        }
        BasicText(r.dur, Modifier.padding(end = 10.dp), style = TextStyle(GlassTheme.Content.copy(0.55f), 12.sp))
        Box(
            Modifier.size(34.dp).clip(CircleShape)
                .background(if (r.added) accent.copy(0.30f) else Color.White.copy(0.14f))
                .tap(enabled = !r.added) { c.cmd { put("t", "add"); put("a", "row"); put("i", index) } },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(12.dp)) {
                strokeIcon(if (r.added) GlassIcons.Check else GlassIcons.Plus, 11f, accent, 1.9f)
            }
        }
    }
}


/* ───────────────────────── 플레이리스트 곡 편집 메뉴 (길게 누르기) ───────────────────────── */

internal const val EDIT_MENU_W = 196f
internal const val EDIT_MENU_H = 118f

/** 메뉴의 x (CSS px == dp): 편집 중인 행의 오른쪽 가장자리에 붙인다. */
internal fun editMenuLeft(e: EditState): Float = e.rect.x + e.rect.w - EDIT_MENU_W - 10f

/**
 * 메뉴의 y (화면 좌표, dp). 행 바로 아래가 기본이고, 하단 도크(미니 플레이어/탭)에 가리면 행 위로 뒤집는다.
 * [scroll] 은 현재 문서 스크롤(CSS px) — 행의 문서 좌표에서 빼서 화면 좌표로 바꾼다.
 */
internal fun editMenuTop(e: EditState, s: GlassState, scroll: Float): Float {
    val rowTop = e.rect.y - scroll
    val rowBottom = rowTop + e.rect.h
    val dockTop = listOfNotNull(s.bar?.rect?.y, s.nav?.rect?.y).minOrNull() ?: e.viewportH
    val limit = dockTop - 8f
    val below = rowBottom + 6f
    if (below + EDIT_MENU_H <= limit) return below
    val above = rowTop - 6f - EDIT_MENU_H
    if (above >= 8f) return above
    return (limit - EDIT_MENU_H).coerceAtLeast(8f)
}

/**
 * Kyant `DialogContent` 와 같은 Liquid Glass(colorControls + blur + lens + plain highlight) 컨텍스트 메뉴.
 * 보이는 조건 = JS 가 숨기지 않았고(드래그 중 아님) + 스크롤 중이 아님. 스크롤이 멈추면 정확한 위치에서 스프링으로 다시 나타난다.
 * 숨겨진 동안에는 컴포지션에서 빠져 터치를 가로채지도, 블러/렌즈 연산을 하지도 않는다.
 */
@Composable
internal fun EditMenuGlass(e: EditState, s: GlassState, backdrop: Backdrop, c: GlassController) {
    val pop = remember { Animatable(0f) }
    val show = e.visible && !c.scrolling
    LaunchedEffect(show) { pop.animateTo(if (show) 1f else 0f, spring(0.72f, 420f, 0.001f)) }
    if (pop.value < 0.01f && !show) return

    val accent = LocalGlassAccent.current
    val interactive = show && pop.value > 0.6f
    Box(
        Modifier
            .offset {
                IntOffset(
                    (editMenuLeft(e) * density).roundToInt(),
                    (editMenuTop(e, s, c.cssScroll(density)) * density).roundToInt()
                )
            }
            .size(EDIT_MENU_W.dp, EDIT_MENU_H.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .dialogGlass(backdrop, 30, pop.value, 16f, 32f)
                .then(NoTouchThrough)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            EditMenuRow(
                label = e.favLabel,
                color = if (e.fav) accent else GlassTheme.Content,
                enabled = interactive,
                icon = {
                    val tint = if (e.fav) accent else GlassTheme.Content
                    Canvas(Modifier.size(20.dp)) {
                        if (e.fav) fillIcon(GlassIcons.StarPath, 20f, tint.copy(alpha = 0.35f))
                        strokeIcon(GlassIcons.Star, 20f, tint, 1.6f)
                    }
                }
            ) { c.cmd { put("t", "edit"); put("a", "fav") } }
            Box(
                Modifier.padding(horizontal = 10.dp).fillMaxWidth().height(0.5.dp)
                    .background(Color.White.copy(0.18f))
            )
            EditMenuRow(
                label = e.deleteLabel,
                color = GlassTheme.Danger,
                enabled = interactive,
                icon = {
                    Canvas(Modifier.size(20.dp)) { strokeIcon(GlassIcons.Trash, 20f, GlassTheme.Danger, 1.6f) }
                }
            ) { c.cmd { put("t", "edit"); put("a", "del") } }
        }
    }
}

@Composable
private fun EditMenuRow(
    label: String, color: Color, enabled: Boolean,
    icon: @Composable () -> Unit, onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .height(49.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (pressed) Color.White.copy(0.14f) else Color.Transparent)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = { if (enabled) { pressed = true; tryAwaitRelease(); pressed = false } },
                    onTap = { if (enabled) onClick() }
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        BasicText(
            label, Modifier.padding(start = 12.dp),
            style = TextStyle(color, 15.sp, FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}
