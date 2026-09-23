<template>
  <div class="table-toolbar">
    <slot name="left" />
    <div class="toolbar-right">
      <slot name="right" />
    </div>
  </div>
</template>

<style scoped>
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
