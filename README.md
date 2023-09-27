# 项目概述
随着移动支付的盛行，商业银行、第三方支付公司、其它清算机构、消费金融公司等众多类型的机构，都在为商户提供网络(移动)支付解决方案。另一方面，用户的支付需求繁多，支付渠道已呈“碎片化”状态，并且“碎片化”程度将逐渐加深，聚合支付顾名思义就是将目前主流的支付进行整合，形成第三方支付的聚合通道，也被称为“第四方支付”

本项目是一个提供聚合支付的平台，聚合支付目前主要的做法就是线上聚合收银台(开放API)，线下C2B一码多付，平台应以SaaS服务形式提供给各商户门店管理、财务数据统计等基础服务，还可以以支付为入口，通过广告、营销、金融等服务（待开发），构建一个移动支付的全生态系统

# 业务流程

<img width="875" alt="image" src="https://github.com/GJZ0117/shanjupay/assets/49833979/a279e502-80fe-4a8b-bc4e-d9020dc126a6">

1. 商户注册平台账号并登陆
2. 提交资质信息进行认证
3. 平台审核商户资质信息
4. 商户创建支付应用
5. 商户填写支付渠道参数(支付宝、微信、银联等) 
6. 商户测试支付渠道后上线使用
7. C端用户在商户消费并进行二维码支付
8. 商户可通过平台浏览交易数据

# 主要技术栈
+  前端：vue、typescript
+  后端：Java、Spring Boot、Spring Cloud Alibaba、MySQL、Redis、RocketMQ、qiniu-CDN

# 项目启动
## 前端项目启动
+  在命令行中进入 `project-juhezhifu-admin-vue` 目录
+  输入 `export NODE_OPTIONS=--openssl-legacy-provider`
+  编译并运行 `npm run build` 和 `npm run serve`
## 后端项目启动
+  docker中依次启动 MySQL、Nacos、Redis、RocketMQNameserver、RocketMQBroker
+  在命令行中输入 `docker ps` 查看RocketMQBroker对应的id，然后输入 `docker exec -it 容器id /bin/bash`进入容器内，使用 `vim ../conf/broker.conf` 修改配置中的ip（mac上ip查询方法：命令行 `ifconfig` 找到 eth0中的inet），修改完后保存退出容器并重启RocketMQBroker
+  在Nacos配置管理中的dev下的 `transaction-service.yaml` 支付入口url的ip改为刚查询到的ip
+  IDEA中依次启动 sailing、gateway、user、uaa、transaction、paymentAgent、merchant、merchantApplication
