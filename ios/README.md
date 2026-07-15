# Vfm Physics Lab iOS

使用 Xcode 16 或更高版本打开 `VfmPhysicsLab.xcodeproj`，选择开发团队与 iPad 模拟器或真机后运行。工程最低支持 iOS 15，界面固定为横屏。

右侧 3×4 面板可点击或拖动添加物体。长按画布中的物体打开完整属性面板。左侧工具依次为普通拖动、初速度、施力、绘制天花板、切割和擦除。底部提供全局设置、播放、清空、重计和 60 秒时间轴，重计仅将当前状态的计时归零。

无签名模拟器构建命令：

```bash
xcodebuild -project VfmPhysicsLab.xcodeproj -scheme VfmPhysicsLab -sdk iphonesimulator -configuration Debug CODE_SIGNING_ALLOWED=NO build
```

项目根目录包含 GitHub Actions macOS 构建流程 `.github/workflows/ios-unsigned-ipa.yml`。将完整项目提交到 GitHub 后，在 Actions 页面手动运行 `Build iOS IPA`，成功后可下载 `VfmPhysicsLab-unsigned-ipa`。无签名 IPA 需要使用 Sideloadly、AltStore 或自己的 Apple 开发证书重新签名后才能安装。
