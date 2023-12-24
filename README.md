# 权限申请框架

PermissionActivity（ChinaPermissionActivity）作为父类的权限申请框架

## 1.2.4更新

- 调整开始方法的响应方式，将延迟放到底层来做。

## 1.2.3更新

- 调整结束回调响应位置，让他响应速度更快

## 1.2.2更新

- 增加权限发起结束的回调

## 1.2.1更新

- 增加中国权限模块检查权限是否存在48小时内重复请求的方法`checkPermissions()`，如果所有权限通过则回调`permissionCheck48HPass()`,如果存在48小时内重复请求权限则回调`permissionCheck48HDenied(var list)`主要用来解决Android14以后，自带权限请求的三方相册lib库，本身会因为权限请求拉起系统相册，如果开发人员选择前置自行进行权限请求，会出现重复拉起系统相册的问题，所以只能前置进行权限48小时检查。该问题，一般只在OPPO和Vivo等子子孙孙平台上线会被要求(Fuck OPPO,Fuck Vivo)，默认的权限请求模块逻辑并不需要这个东西。默认遵照Android的原始逻辑来走。

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
