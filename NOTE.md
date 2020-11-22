# Zookeeper
## 1. Zookeeper简介
1. 简介
   ```
   使用场景：分布式系统的分布式协同服务。

   分布式系统定义：分布式系统是同时跨越多个物理机，独立运行的多个软件所组成的系统。
   ```
   
2. Zookeeper的数据同步方式
   ```
   数据同步方式有两种：共享网络通信同步、共享存储同步
   
   Zookeeper使用共享存储同步
   ```
   
3. Zookeeper的基本概念
   ```
   1. 集群角色
      Leader：  
          为客户端提供读和写服务
      
      Follower：
         为客户端提供读服务
         如果接受到写请求则会转发给Leader
         参与Leader选举过程与写操作过半即成功策略
      
      Observer：
         为客户端提供读服务
         不参与Leader选举过程与写操作过半即成功策略
         可以在不影响写性能的情况下提升集群性能
   
   2. 会话（Session）
      Session指客户端会话，一个客户端连接是指客户端和服务端之间的一个TCP长连接
	  
      客户端：
         能够心跳检测与服务器保持有效的会话
         能够向服务端发送请求并接受响应
         能够通过该连接接受来自服务器的Watch事件
   
   3. 数据节点（Znode）
      节点是指组成集群的每一台机器
	  
      节点类型：
          1. 机器节点：构成集群的机器
          2. 数据节点：
                数据模型中的数据单元称之为数据节点----ZNode
                数据存储在内存中，数据模型是一个树（ZNode Tree），由斜杠（/）进行路径分割
	  
   4. 版本
      Zookeeper为每个ZNode维护了一个Stat数据结构，Stat记录了这个ZNode的三个数据版本。
          1. Version： 当前ZNode的版本
          2. CVersion：当前ZNode子节点的版本
          3. AVersion：当前ZNode的ACL版本		  
   
   5. Watcher（事件监听器）
      Watcher是客户端在Zookeeper指定节点上注册的监听事件，当在指定的ZNode上触发了指定事件，那么Zookeeper会将其发送给客户端
      该机制是Zookeeper实现分布式协调服务的重要特性
   
   6. ACL（Access Control Lists）
      Zookeeper使用ACL进行权限控制：
         CREATE： 创建子节点的权限
         READ：   获取节点数据和子节点列表的权限
         WRITE：  更新节点数据的权限
         DELETE： 删除子节点的权限
         ADMIN：  设置节点ACL的权限
   ```
   
## 2. Zookeeper基本使用
1. ZNode类型
   ```
   Zookeeper节点类型分为三大类：持久性节点（Persistent）、临时性节点（Ephemeral）、顺序性节点（Sequential）
   
   由三大节点类型组合成下面四种节点类型：
       1. 持久节点：     
          指节点被创建后会一直存在服务器上，直到删除操作主动清楚
			 
       2. 持久顺序节点： 
          指有顺序的持久节点，节点特性和持久节点一样，只是多了有序的特性。
          顺序特性的实质是在创建节点的时候，节点名后面加了一个数字后缀来表示顺序。
			 
       3. 临时节点：
          指会被自动清理掉的节点，它的生命周期与客户端会话绑在一起，客户端会话结束，节点就会被删除
          与持久性节点的不同是临时节点不能创建子节点
			 
       4. 临时顺序节点：
          指有顺序的临时节点，顺序与持久节点一致。
   ```

2. 事务ID
   ```
   事务是对物理和抽象应用状态上的操作集合。
   
   在Zookeeper中事务是指能够改变Zookeeper服务器状态的操作，也称之为事务操作或更新操作。
   一般包括数据节点的创建、删除、更新等操作。
   
   对于每一个事务请求，Zookeeper都会为其分配一个全局唯一的事务ID，用ZXID表示，通常是一个64位数字。
   每个ZXID对应一次更新操作，从ZXID中可以间接识别出Zookeeper处理这些更新操作请求的全局顺序。
   ```
   
3. ZNode状态信息
   ```
   ZNode节点内容包括两部分：节点数据内容、节点状态信息
   
   节点状态信息含义:
      cZxid：             Create ZXID，表示节点被创建时的事务ID
      ctime：             Create Time，表示节点的创建时间 
      mZxid：             Modified ZXID，表示节点最后一次被修改时的事务ID 
      mtime：             Modified Time，表示节点最后一次被修改的时间
      pZxid：             表示该节点的子节点列表最后一次被修改时的事务ID，只有子节点列表变更才会更新pZxid，子节点内容变更不会更新
      cversion：          表示子节点的版本
      dataVersion：       表示数据（内容）版本
      aclVersion：        表示acl版本
      ephemeralOwner：    表示创建该临时节点时的会话SessionID，如果是持久性节点，则为0
      dataLength：        表示数据长度
      numChildren：       表示直系子节点的个数
   ```

4. Watcher--数据变更通知
   ```
   Zookeeper使用Watcher机制实现分布式数据的发布/订阅功能
   
   Zookeeper的Watcher机制主要包括：客户端线程、客户端WatcherManager、Zookeeper服务器三部分。
   工作流程：
      1. 客户端向Zookeeper服务器注册Watcher
      2. 客户端将注册的Watcher保存到客户端的WatcherManager当中
      3. Zookeeper服务端触发Watcher，并向客户端发送通知
      4. 客户端从WatcherManager当中取出对应的Watcher，并执行回调逻辑。
   ```
   
5. ACL--保障数据的安全：
   ```
   三个方面理解ACL机制：权限模式（Scheme）、授权对象（id）、权限（Permission），通常使用“scheme:id:permission”来标识一个有效的ACL信息。
   
   权限模式：Schema
      权限模式用来确定权限验证过程中使用的验证策略
	  
	  1. IP
	     IP模式就是通过IP地址粒度来进行权限控制
		 
	  2. Digest
	     Digest是最常用的权限控制模式，更符合对权限控制的认识，使用"username:password"的形式进行权限控制
	  
	  3. World
	     World是一种最开放的权限控制模式，这种权限控制模式几乎没有任何作用
	  
	  4. Super
	     Super模式也是一种特殊的控制模式，只有超级用户才有权限。
		 
   授权对象：ID
      授权对象指的是权限赋予的用户或一个指定实体
         IP ---- 通常是一个IP或IP端，例如：192.168.1.1或192.168.1.1/24
         Digest ---- 自定义，通常是username:BASE64(SHA-A(username:password))
         World ---- 只有一个ID：anyone
         Super ---- 超级用户
    
   权限：Permission
      CREATE、READ、WRITE、DELETE、ADMIN 	
   ``` 