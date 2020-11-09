# 简介
## *油气田能效查询组件demo*
基于kettle源码开发出的kettle自定义组件，以jar的形式放在spoon插件目录下，在开发时作为自定义组件来使用。当前开发的是Kettle中**步骤**类型组件。

## 1 功能概述
* #### 页面进行组件的查询条件及查询字段的配置
* #### 流程执行过程，读取配置，查询出相应的油气田能效
## 2  组件设计
### 2.1 页面设计
#### 2.1.1 上方三个选择框及浏览按钮，进行油气田用能层级对象的选择。
* 初始展示上次编辑后保存的配置。
* 点击每个浏览按钮，组件会查询用能对象的层级（通过模型服务接口），并展示出用能层级列表。
* 用能层级列表可以进行单选或多选，点击确认，选择好的对象会放到文本框中。取消则不更新。
#### 2.1.2 中间部分，查询出的具体能效字段。
* 初始展示的上次编辑后保存的配置，包括能效字段名，能效字段类型。
* 基于kettle本身的配置，选中某一行点击Delete，可以删除某个字段，即表示查询能效的时候不需要查询该字段。
#### 2.1.3 最下方按钮
* **确定按钮**。将本次页面编辑的内容序列化到xml文件中。
* **获取字段**。将通过模型服务元数据接口查询到上方选中条件的模型的元数据，设计的三个层级分别为**作业区**、**平台**、**机采设备**。三个层级，查询元数据时候会取一个模型的元数据进行查询。查询时候，当时底层**机采设备**条件为空，则取上层设备。如果都为空，则会弹出提示框。
### 2.2 基于页面的逻辑设计
#### 2.2.1 依赖的接口功能
##### 模型服务查询
模型服务之前都是通过微服务查询，而实际开发过程中kettle不在微服务网络环境中，所以不能通过feign接口来调用，原生或者apache的工具都需要封装调用和解析的过程，所以使用restTemplate工具来实现接口调用和解析。
###### 1. 查询单个模型层级数据
* 函数入参
	* **String modelLabel** --> 模型label条件
	* **List\<Long\> ids** --> 模型ID列表条件
* 函数返回
	* **List\<? extends ModelObject\> modelObjects** --> 模型层级数据的实体列表
###### 2. 查询模型服务元数据
* **函数入参**
    * **String modelLabel** --> 模型label条件
* **函数返回**
    * List\<ModelProperties\> --> 单个模型的属性元列表，包含字段名，字段类型等重要信息。
###### 3. 流程执行时候，查询模型服务数据
* **函数入参**
	* **String modelLable** --> 模型label条件
	* **List\<Long\> ids** --> 模型ID列表条件
	* **List<\String\>** --> 要查询的字段名
* **函数返回值**
	* **List\<ObjectEnergyEff\> objectEffsList**
		* 每一个ObjectEnergyEff是一个对象一个时段的多种能效值的一条记录。
#### 2.2.2 配置的存取
kettle步骤的内部存储基本上是通过xml来完成的。在StepMetaInterface接口中提供了两个方法定义的多个重载接口。

1. 加载配置信息（第一个方法过时了）
应该是pdi的框架上定义好，当流程运行时候会先读取到步骤、转换、作业的信息到内存，其中的信息读取依赖于StepMetaInterface等元数据接口。

```java
@Deprecated
void loadXML( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters ) throws KettleXMLException;

void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException;
```
2. 写入配置信息
```java
String getXML() throws KettleException;
```
基于这两类接口，我们可以将步骤的元数据编辑过程中用到的数据存放于Meta类的域字段。在具体加载或写入的方法中，定义操作逻辑，即可完成读写。

#### 2.2.3 流程执行过程

TODO


# 入门
## 1. 安装过程
* 下载源码
将项目源码

* 加入PDI大的maven项目中
将该项目加入到PDI项目中，加到路径：**\\plugins\\**下。

* 将环境构建打包。
	* 主要是对maven的依赖进行下载，然后构建。
	* 需要的依赖主要有三方面。
		1. kettle方面的依赖。
		2. maven中央仓库的依赖。 
		3. 公司内部的jar包依赖。
	* 环境构建是maven命令完成，中途不能修改配置，而必须从多个仓库下载到依赖，所以，需要进行maven配置文件setttings.xml的配置。在公司私库的基础上加入kettle的镜像与仓库。xml配置的要点有：
		* 将kettle镜像和仓库和公司的区别开，要建立一个新的profile节点，并激活，而不是在后面加入kettle的仓库。如下，激活公司和kettle外部的仓库配置。节点路径：**settings -> activeProfiles**
		
		  ```xml
		  <activeProfiles
		      <activeProfile>dev</activeProfile>
		      <activeProfile>pentaho</activeProfile>
		  </activeProfiles>
		  ```
		
		* 并且在镜像时候排除公司私库的地址。如下，节点路径 **settings -> mirrors -> mirror**
		
		  ```xml
		  <mirror>
		  	<id>pentaho-public</id>
		  	<url>https://repo.orl.eng.hitachivantara.com/artifactory/pnt-mvn/</url>
		  	<mirrorOf>*,!nexus</mirrorOf>
		  </mirror>
		  ```
		
		* 配置仓库与插件仓库
		
		  * 仓库节点 **settings -> profiles -> profile -> repositories**
		  * 插件仓库节点 **settings -> profiles -> profile -> pluginRepositories**
		
		* maven完整文件链接给到，注意修改本地maven仓库路径。
[settings.xml](.\settings.xml)

## 2. 软件依赖项
## 3. 最新发布
## 4. API 参考

# 生成与测试




# 参与
TODO: 说明其他用户和开发人员可如何帮助改善代码。

如需深入了解如何创建优秀的自述文件，请参阅以下[指南](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops)。还可从以下自述文件中寻求灵感:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)