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
// 核心类
IdentityNumber     - 身份证号码封装类
IdentityCodec      - 统一编码接口

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

### 身份证验证规则

`IdentityNumber` 类实现了严格的身份证号码验证：

**基本格式验证**：
- 长度必须为18位
- 前17位必须为数字
- 第18位可以是数字或大写字母X

**语义验证**：
- 地址码：000000-999999
- 年份：0000-9999
- 月份：01-12
- 日期：根据月份和闰年验证具体天数
- 顺序码：000-999
- 校验码：根据国家标准GB 11643计算验证

**特殊处理**：
- 自动将小写x转换为大写X
- 严格的日期有效性检查（包括闰年判断）

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
    // 创建基础编码器（推荐使用工厂方法）
    IdentityCodec codec = IdentityCodecs.simple();

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

### 最新基准测试结果 (100万次迭代)

| 操作类型 | 平均耗时 | 吞吐量 | 测试次数 |
|---------|---------|--------|----------|
| 身份证解析 | 29.13ns | 34,327,810 ops/sec | 1,000,000 |
| 普通编码 | 15.72ns | 63,597,858 ops/sec | 1,000,000 |
| 普通解码 | 35.71ns | 28,003,282 ops/sec | 1,000,000 |
| 加密编码 | 33.32ns | 30,008,042 ops/sec | 500,000 |
| 加密解码 | 72.31ns | 13,830,226 ops/sec | 500,000 |
| 批量处理 | 28.33ns | 35,296,651 ops/sec | 2,000,000 |

### 并发性能测试

| 测试项 | 结果 |
|--------|------|
| 并发线程数 | 10个 |
| 每线程操作数 | 100,000 |
| 总吞吐量 | 11,177,943 ops/sec |
| 平均响应时间 | 89.46ns |

### 压力测试结果

| 测试项 | 结果 |
|--------|------|
| 总操作次数 | 10,000,000 |
| 总耗时 | 746ms |
| 平均响应时间 | 74.69ns |
| 吞吐量 | 13,388,851 ops/sec |

### 内存效率

- **IdentityNumber对象**：约84字节（10万个对象测试）
- **编码后数据**：8字节（long类型）
- **内存增长**：创建10万个对象增加约8MB内存
- **内存优化**：内部缓存原始字符串，避免重复解析

### 存储效率

- **原始大小**：18字符 × 1字节 = 18字节
- **编码后**：8字节 (long类型)
- **压缩率**：55.6% 减少

### 日期范围

- **基准日期**：0000年1月1日
- **最大支持**：约11485年的时间跨度
- **天数位数**：22位，可表示4,194,304天

### 性能特点

- **超低延迟**：核心操作在15-72纳秒范围内
- **超高吞吐量**：单线程可达6300万ops/sec
- **良好并发性**：多线程环境下性能稳定
- **内存友好**：每个对象占用内存控制在合理范围内

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

#### IdentityNumber (核心类)
身份证号码的封装类，提供身份证号码的解析、验证和格式化功能。

```java
// 解析身份证号码
IdentityNumber id = IdentityNumber.parse("11010519491231002X");

// 格式化身份证号码
IdentityNumber id = IdentityNumber.format(110105, 1949, 10, 1, 1);

// 获取身份证信息
int address = id.address();    // 地址码
short year = id.year();        // 年份
byte month = id.month();       // 月份
byte day = id.day();           // 日期
short sequence = id.sequence(); // 顺序码
```

#### IdentityCodec (接口)
统一的身份编码接口，定义编码和解码标准方法。

#### IdentityCodecs (工厂类)
提供创建各种编码器实例的静态工厂方法。

```java
// 创建简单编码器（无加密）
IdentityCodec simpleCodec = IdentityCodecs.simple();

// 创建SPECK64加密编码器
IdentityCodec encryptedCodec = IdentityCodecs.speck64Encrypt(new int[]{1, 2, 3, 4});
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
        // 身份证号码解析异常
        IdentityNumber id = IdentityNumber.parse("invalid_id_card");
    } catch (IdentityNumberFormatException e) {
        // 处理无效身份证格式（长度、字符、校验码等）
        System.err.println("身份证格式错误: " + e.getMessage());
    }

    try {
        // 编码异常
        long encoded = codec.encode(IdentityNumber.parse("11010519491231002X"));
    } catch (IdentityCodecException e) {
        // 处理编码错误（版本不支持、数据格式错误等）
        System.err.println("编码错误: " + e.getMessage());
    }

    try {
        // 解码异常
        IdentityNumber decoded = codec.decode(invalidEncodedValue);
    } catch (IdentityCodecException e) {
        // 处理解码错误
        System.err.println("解码错误: " + e.getMessage());
    }
}
```

## 开发指南

### 构建项目

```bash
# 编译项目
mvn compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=PerformanceBenchmarkTest

# 运行特定测试方法
mvn test -Dtest=PerformanceBenchmarkTest#testEncodingPerformance

# 打包发布
mvn package
```

### 性能测试

项目包含专门的性能基准测试类 `PerformanceBenchmarkTest`，可以测试：

- 身份证号码解析性能
- 编码解码性能
- 加密解密性能
- 批量处理性能
- 内存使用情况
- 并发处理能力
- 压力测试

```bash
# 运行性能测试
mvn test -Dtest=PerformanceBenchmarkTest

# 查看详细性能报告
mvn test -Dtest=PerformanceBenchmarkTest -X
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
- 所有公共方法必须包含 Javadoc 注释
- 单元测试覆盖率不低于85%
- 提交前运行所有测试确保通过
- 新增功能需提供相应的单元测试

---

**注意**：本库仅用于身份证号码的编码压缩，不涉及个人隐私数据的存储和传输，请在合规的前提下使用。