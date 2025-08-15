import { reactive } from 'vue'
import { toast } from '~/main'

export const ThemeMode = {
  SYSTEM: 'system',
  LIGHT: 'light',
  DARK: 'dark',
}

const THEME_KEY = 'theme-mode'

export const themeState = reactive({
  mode: ThemeMode.SYSTEM,
})

function apply(mode) {
  if (!import.meta.client) return
  const root = document.documentElement
  let newMode =
    mode === ThemeMode.SYSTEM
      ? window.matchMedia('(prefers-color-scheme: dark)').matches
        ? 'dark'
        : 'light'
      : mode
  if (root.dataset.theme === newMode) return
  root.dataset.theme = newMode

  // 更新 meta 标签
  const androidMeta = document.querySelector('meta[name="theme-color"]')
  const iosMeta = document.querySelector('meta[name="apple-mobile-web-app-status-bar-style"]')
  const themeColor = getComputedStyle(document.documentElement)
    .getPropertyValue('--background-color')
    .trim()
  const themeStatus = newMode === 'dark' ? 'black-translucent' : 'default'

  if (androidMeta) {
    androidMeta.content = themeColor
  } else {
    const newAndroidMeta = document.createElement('meta')
    newAndroidMeta.name = 'theme-color'
    newAndroidMeta.content = themeColor
    document.head.appendChild(newAndroidMeta)
  }

  if (iosMeta) {
    iosMeta.content = themeStatus
  } else {
    const newIosMeta = document.createElement('meta')
    newIosMeta.name = 'apple-mobile-web-app-status-bar-style'
    newIosMeta.content = themeStatus
    document.head.appendChild(newIosMeta)
  }
}

export function initTheme() {
  if (!import.meta.client) return
  const saved = localStorage.getItem(THEME_KEY)
  if (saved && Object.values(ThemeMode).includes(saved)) {
    themeState.mode = saved
  }
  apply(themeState.mode)
}

export function setTheme(mode) {
  if (!import.meta.client) return
  if (!Object.values(ThemeMode).includes(mode)) return
  themeState.mode = mode
  localStorage.setItem(THEME_KEY, mode)
  apply(mode)
}

function getCircle(event) {
  if (!import.meta.client) return undefined

  let x, y
  if (event.touches?.length) {
    x = event.touches[0].clientX
    y = event.touches[0].clientY
  } else if (event.changedTouches?.length) {
    x = event.changedTouches[0].clientX
    y = event.changedTouches[0].clientY
  } else {
    x = event.clientX
    y = event.clientY
  }

  return {
    x,
    y,
    radius: Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y)),
  }
}

function withViewTransition(event, applyFn, direction = true) {
  if (typeof document !== 'undefined' && document.startViewTransition) {
    const transition = document.startViewTransition(async () => {
      applyFn()
      await nextTick()
    })

    transition.ready
      .then(() => {
        const { x, y, radius } = getCircle(event)

        const clipPath = [`circle(0 at ${x}px ${y}px)`, `circle(${radius}px at ${x}px ${y}px)`]

        document.documentElement.animate(
          {
            clipPath: direction ? clipPath : [...clipPath].reverse(),
          },
          {
            duration: 400,
            easing: 'ease-in-out',
            pseudoElement: direction
              ? '::view-transition-new(root)'
              : '::view-transition-old(root)',
          },
        )
      })
      .catch(console.warn)
  } else {
    applyFn()
  }
}

function getSystemTheme() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function cycleTheme(event) {
  if (!import.meta.client) return
  const modes = [ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK]
  const index = modes.indexOf(themeState.mode)
  const next = modes[(index + 1) % modes.length]
  if (next === ThemeMode.SYSTEM) {
    toast.success('💻 已经切换到系统主题')
  } else if (next === ThemeMode.LIGHT) {
    toast.success('🌞 已经切换到明亮主题')
  } else {
    toast.success('🌙 已经切换到暗色主题')
  }
  // 获取当前真实主题
  const currentTheme = themeState.mode === ThemeMode.SYSTEM ? getSystemTheme() : themeState.mode

  // 获取新主题的真实表现
  const nextTheme = next === ThemeMode.SYSTEM ? getSystemTheme() : next

  // 如果新旧主题相同，不用过渡动画
  if (currentTheme === nextTheme) {
    setTheme(next)
    return
  }

  // 计算新主题是否是暗色
  const newThemeIsDark = nextTheme === 'dark'

  withViewTransition(
    event,
    () => {
      setTheme(next)
    },
    !newThemeIsDark,
  )
}

if (import.meta.client && window.matchMedia) {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (themeState.mode === ThemeMode.SYSTEM) {
      apply(ThemeMode.SYSTEM)
    }
  })
}
