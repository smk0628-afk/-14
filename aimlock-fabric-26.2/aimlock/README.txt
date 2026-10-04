AimLock (Fabric, Minecraft 26.2)
Z 키: 에임 고정 켜기/끄기 (키 설정에서 변경 가능)

[빌드 방법]
1) JDK 25 설치
2) Gradle 9.5.1 설치 후 이 폴더에서:
     gradle wrapper --gradle-version 9.5.1
     ./gradlew build        (윈도우: gradlew.bat build)
3) build/libs/aimlock-1.0.0.jar 를 mods 폴더에 넣기
   (Fabric Loader 0.19.3+ 와 Fabric API 0.152+ 필요)

[설정]
src/main/java/com/example/aimlock/AimLockClient.java 상단의
RANGE, TARGET_MONSTERS, TARGET_PLAYERS, SMOOTHNESS 값을 바꾸면 됩니다.
