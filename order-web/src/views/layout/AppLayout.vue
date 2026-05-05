<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="app-aside">
      <div class="logo-container">
        <el-icon :size="28" color="#409EFF"><Food /></el-icon>
        <span v-show="!isCollapse" class="logo-text">外卖订单系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
        class="app-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <template #title>订单列表</template>
        </el-menu-item>
        <el-menu-item index="/orders/create">
          <el-icon><CirclePlus /></el-icon>
          <template #title>创建订单</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="app-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag type="success" effect="plain" class="balance-tag">
            <el-icon style="margin-right: 4px;"><Wallet /></el-icon>
            余额：{{ userStore.formattedBalance }}
          </el-tag>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" style="background-color: #409EFF;">
                <el-icon :size="18"><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  用户ID：{{ userStore.userId }}
                </el-dropdown-item>
                <el-dropdown-item divided disabled>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const route = useRoute()
const isCollapse = ref(false)

const activeMenu = computed(() => {
  const path = route.path
  // 订单详情和支付页面高亮"订单列表"
  if (path.startsWith('/orders/') && path !== '/orders/create') {
    return '/orders'
  }
  if (path.startsWith('/payment/')) {
    return '/orders'
  }
  return path
})

const currentRoute = computed(() => route)
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.app-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #ffffff1a;
  padding: 0 16px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-text {
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.app-menu {
  border-right: none;
}

.app-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
}

.main-container {
  background-color: #f0f2f5;
}

.app-header {
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.balance-tag {
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.username {
  font-size: 14px;
}

.app-main {
  padding: 20px;
  overflow-y: auto;
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
