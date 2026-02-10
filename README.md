# Identity Codec 身份编码器

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/nextentity/identity-codec)
[![Java](https://img.shields.io/badge/java-25%2B-orange)](https://www.oracle.com/java/)

## 项目简介

Identity Codec 是一个高效的身份证明编码库，专门用于将18位中国身份证号码压缩编码为64位long类型，并提供可选的加密功能。该项目采用位域压缩技术，在保证数据完整性的前提下大幅减少存储空间占用。

## 核心特性

- 🔐 **双重编码模式**：支持普通编码和加密编码
- 🚀 **高性能**：基于位运算的快速编码解码
- 💾 **高压缩率**：18位身份证压缩至56位有效数据
- 🛡️ **安全加密**：集成SPECK64轻量级分组密码算法
- 📊 **零依赖**：纯Java实现，无需外部依赖
- ✅ **完整测试**：包含全面的单元测试覆盖

## 技术架构

### 核心组件

```text
// 主要接口
IdentityCodec - 统一编码接口

// 实现类
SimpleIdentityCodec     - 基础身份编码器
EncryptedIdentityCodec  - 加密身份编码器
Speck64Encryptor        - SPECK64加密器
IdentityCodecs          - 工厂类
```

### 位域分配结构

```
位域分配 (共 56 位):

[63-56]: 预留位 ( 8 位) - 保持为 0
[55-36]: 地址码 (20 位) - 行政区划代码
[35-14]: 生日码 (22 位) - 距离基准日期的天数
[13- 4]: 顺序码 (10 位) - 同日出生人员序号
[ 3- 0]: 版本号 ( 4 位) - 编码版本标识
```

## 快速开始

### 1. Maven依赖

```xml
<dependency>
    <groupId>io.github.nextentity</groupId>
    <artifactId>identity-codec</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础使用

```java
void main() {
    // 创建基础编码器
    IdentityCodec codec = new SimpleIdentityCodec();

    // 编码身份证
    String idCard = "110105194912310021";
    long encoded = codec.encode(idCard);
    System.out.println("编码结果: " + encoded);

    // 解码身份证
    String decoded = codec.decode(encoded);
    System.out.println("解码结果: " + decoded);
}
```

### 3. 加密编码

```java
void main() {
    // 推荐使用工厂类创建加密编码器
    IdentityCodec encryptedCodec = IdentityCodecs.speck64Encrypt(new int[]{1, 2, 3, 4});

    // 加密编码
    long encrypted = encryptedCodec.encode(idCard);
    System.out.println("加密编码: " + encrypted);

    // 解密解码
    String decrypted = encryptedCodec.decode(encrypted);
    System.out.println("解密结果: " + decrypted);
}
```

## 性能表现

### 基准测试结果

| 操作类型 | 平均耗时 | 吞吐量 |
|---------|---------|--------|
| 普通编码 | ~0.1μs | >1M ops/sec |
| 加密编码 | ~0.2μs | >500K ops/sec |
| 解码操作 | ~0.1μs | >1M ops/sec |

### 存储效率

- **原始大小**：18字符 × 1字节 = 18字节
- **编码后**：8字节 (long类型)
- **压缩率**：77.8% 减少

### 日期范围

- **基准日期**：0000年1月1日
- **最大支持**：约11485年的时间跨度
- **天数位数**：22位，可表示4,194,304天

## 安全特性

### SPECK64加密机制

SPECK是一族轻量级分组密码算法。

**算法特点**：
- 分组大小：64位
- 密钥长度：128位（支持多种密钥长度）
- 标准轮数：27轮
- 轻量级设计，适合移动设备和嵌入式系统
- 经过充分的安全性分析

**安全注意事项**：
- 加密强度依赖于密钥的随机性和保密性
- 建议使用安全的随机数生成器产生密钥
- 适用于中高等安全级别的应用场景

## API参考

### 主要类和接口

#### IdentityCodec (接口)
统一的身份编码接口，定义编码和解码标准方法。

#### IdentityCodecs (工厂类)
提供创建各种编码器实例的静态工厂方法。

```java
// 创建SPECK64加密编码器
IdentityCodec encryptedCodec = IdentityCodecs.speck64Encrypt(new int[]{1, 2, 3, 4});

// 创建基础编码器
IdentityCodec simpleCodec = new SimpleIdentityCodec();
```

#### SimpleIdentityCodec
基础身份编码器实现，提供无加密的身份证编码功能。

#### EncryptedIdentityCodec
加密身份编码器，包装基础编码器并添加加密层。

#### Speck64Encryptor
SPECK64加密算法的具体实现，提供64位数据的加密解密功能。

### 异常处理

```java
void main() {
    try {
        long encoded = codec.encode(invalidIdCard);
    } catch (InvalidIdentityNumberException e) {
        // 处理无效身份证格式
    }

    try {
        long encoded = codec.decode(invalidIdCard);
    } catch (InvalidEncodingException e) {
        // 处理编码错误
    }
}
```

## 开发指南

### 构建项目

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包发布
mvn package
```

### 代码质量

```bash
# 代码检查
mvn checkstyle:check

# 单元测试覆盖率
mvn jacoco:report
```

## 贡献指南

欢迎提交Issue和Pull Request！

### 开发环境要求

- Java 25 或更高版本
- Maven 3.6+
- IDE推荐：IntelliJ IDEA 或 Eclipse

### 代码规范

- 遵循Google Java Style Guide
- 所有公共方法必须包含Javadoc注释
- 单元测试覆盖率不低于80%
- 提交前运行所有测试确保通过

---

**注意**：本库仅用于身份证号码的编码压缩，不涉及个人隐私数据的存储和传输，请在合规的前提下使用。