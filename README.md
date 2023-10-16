# 权限申请框架

PermissionActivity（ChinaPermissionActivity）作为父类的权限申请框架

## 1.2.0更新

- 全面适配`Android14`的`READ_MEDIA_VISUAL_USER_SELECTED`权限，解决授权后依旧显示权限拒绝的问题

## 1.1.7更新

- 将代码全部转为kotlin，战未来

## 1.1.5更新

- 修改写法彻底避免出现UnsupportedOperationException

## 1.1.4更新

- 修复UnsupportedOperationException的问题

## 1.0.9更新

- 修复手动授权后，授权弹窗不会消失的问题
- 重要更新！加入ChinaPermissionActivity类，适配国内部分App平台的逗比审核规则。

## 1.0.8更新

- 重要更新！兼容Android13的POST_NOTIFICATIONS权限
