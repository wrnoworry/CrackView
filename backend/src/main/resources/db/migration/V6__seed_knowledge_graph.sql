-- ============================================================
-- 知识图谱种子数据：4大Domain, 80个节点
-- ============================================================

-- ========================
-- Domain: java — Java基础 (25 nodes, id 1-25)
-- ========================

INSERT INTO knowledge_nodes (id, uuid, name, domain, parent_id, depth, description) VALUES
(1,  UUID(), 'Java基础',                'java', NULL, 0, 'Java语言核心知识体系'),
(2,  UUID(), 'JVM',                     'java', 1,    1, 'Java虚拟机原理与调优'),
(3,  UUID(), '内存模型',                'java', 2,    2, 'JVM内存区域划分：堆、栈、方法区、程序计数器'),
(4,  UUID(), '垃圾回收 (GC)',           'java', 2,    2, 'GC算法：标记清除、复制、标记整理；收集器：CMS、G1、ZGC'),
(5,  UUID(), '类加载机制',              'java', 2,    2, '双亲委派模型、类加载过程、自定义ClassLoader'),
(6,  UUID(), 'JIT编译',                 'java', 2,    2, '即时编译、热点代码检测、编译优化'),
(7,  UUID(), 'JVM调优',                 'java', 2,    2, 'JVM参数调优、GC日志分析、内存泄漏排查'),
(8,  UUID(), '并发',                    'java', 1,    1, 'Java并发编程核心'),
(9,  UUID(), '线程池',                  'java', 8,    2, 'ThreadPoolExecutor参数、拒绝策略、线程池最佳实践'),
(10, UUID(), 'synchronized vs ReentrantLock', 'java', 8, 2, '内置锁与显式锁对比、锁升级、公平锁'),
(11, UUID(), 'volatile',                'java', 8,    2, '可见性、有序性、内存屏障'),
(12, UUID(), 'CAS',                     'java', 8,    2, 'Compare-And-Swap原理、ABA问题'),
(13, UUID(), 'AQS',                     'java', 8,    2, 'AbstractQueuedSynchronizer原理、CLH队列'),
(14, UUID(), 'CompletableFuture',       'java', 8,    2, '异步编程、组合式异步操作'),
(15, UUID(), '集合框架',                'java', 1,    1, 'Java集合体系'),
(16, UUID(), 'HashMap原理',             'java', 15,   2, '哈希表、红黑树转换、扩容机制、线程不安全'),
(17, UUID(), 'ConcurrentHashMap',       'java', 15,   2, '分段锁(1.7)、CAS+synchronized(1.8)'),
(18, UUID(), 'ArrayList vs LinkedList', 'java', 15,   2, '数组 vs 链表，随机访问 vs 插入删除性能'),
(19, UUID(), 'TreeMap/红黑树',          'java', 15,   2, '红黑树性质、旋转操作'),
(20, UUID(), 'Spring框架',              'java', 1,    1, 'Spring生态核心知识'),
(21, UUID(), 'IoC/DI',                  'java', 20,   2, '控制反转、依赖注入、Bean生命周期'),
(22, UUID(), 'AOP',                     'java', 20,   2, '面向切面编程、动态代理、CGLib'),
(23, UUID(), 'Spring MVC',              'java', 20,   2, 'DispatcherServlet、请求处理流程'),
(24, UUID(), 'Spring Boot自动配置',     'java', 20,   2, '@EnableAutoConfiguration、条件装配'),
(25, UUID(), 'Spring事务管理',          'java', 20,   2, '声明式事务、传播行为、隔离级别');

-- ========================
-- Domain: distributed — 分布式系统 (20 nodes, id 26-45)
-- ========================

INSERT INTO knowledge_nodes (id, uuid, name, domain, parent_id, depth, description) VALUES
(26, UUID(), '分布式系统',              'distributed', NULL, 0, '分布式系统核心知识体系'),
(27, UUID(), 'CAP定理',                 'distributed', 26,   1, 'Consistency, Availability, Partition tolerance三选二'),
(28, UUID(), '一致性',                  'distributed', 26,   1, '分布式一致性理论与算法'),
(29, UUID(), '强一致性',                'distributed', 28,   2, '线性一致性、顺序一致性'),
(30, UUID(), '最终一致性',              'distributed', 28,   2, 'BASE理论、读写一致性、因果一致性'),
(31, UUID(), 'Raft/Paxos',             'distributed', 28,   2, '分布式共识算法原理与实现'),
(32, UUID(), '分布式事务',              'distributed', 26,   1, '跨服务事务一致性'),
(33, UUID(), '2PC',                     'distributed', 32,   2, '两阶段提交：准备阶段 + 提交阶段'),
(34, UUID(), 'Saga模式',                'distributed', 32,   2, '长事务拆分、补偿机制'),
(35, UUID(), 'TCC',                     'distributed', 32,   2, 'Try-Confirm-Cancel模式'),
(36, UUID(), '消息队列',                'distributed', 26,   1, '异步解耦、削峰填谷'),
(37, UUID(), 'Kafka',                   'distributed', 36,   2, '分区、副本、消费者组、Exactly-Once'),
(38, UUID(), 'RabbitMQ',                'distributed', 36,   2, 'Exchange类型、消息确认、死信队列'),
(39, UUID(), '消息可靠性',              'distributed', 36,   2, '消息丢失、重复消费、顺序消费'),
(40, UUID(), '微服务',                  'distributed', 26,   1, '微服务架构设计'),
(41, UUID(), '服务注册与发现',          'distributed', 40,   2, 'Eureka、Nacos、Consul'),
(42, UUID(), '负载均衡',                'distributed', 40,   2, '客户端/服务端负载均衡、算法'),
(43, UUID(), '熔断降级',                'distributed', 40,   2, 'Hystrix、Sentinel、熔断策略'),
(44, UUID(), 'RPC框架',                 'distributed', 26,   1, 'Dubbo、gRPC、序列化协议'),
(45, UUID(), '分布式ID生成',            'distributed', 26,   1, '雪花算法、UUID、号段模式');

-- ========================
-- Domain: database — 数据库 (20 nodes, id 46-65)
-- ========================

INSERT INTO knowledge_nodes (id, uuid, name, domain, parent_id, depth, description) VALUES
(46, UUID(), '数据库',                  'database', NULL, 0, '数据库核心知识体系'),
(47, UUID(), 'MySQL',                   'database', 46,   1, 'MySQL关系型数据库'),
(48, UUID(), '索引 (B+树)',             'database', 47,   2, 'B+树原理、聚簇/非聚簇索引、索引失效场景'),
(49, UUID(), '事务隔离级别',            'database', 47,   2, '读未提交、读已提交、可重复读、串行化'),
(50, UUID(), 'MVCC',                    'database', 47,   2, '多版本并发控制、Undo Log、ReadView'),
(51, UUID(), '主从复制',                'database', 47,   2, '异步/半同步/全同步复制、binlog'),
(52, UUID(), '分库分表',                'database', 47,   2, '垂直/水平拆分、ShardingSphere、分片策略'),
(53, UUID(), 'SQL优化',                 'database', 47,   2, 'EXPLAIN分析、慢查询优化、索引设计'),
(54, UUID(), 'Redis',                   'database', 46,   1, 'Redis内存数据库'),
(55, UUID(), '数据结构',                'database', 54,   2, 'String、Hash、List、Set、ZSet底层实现'),
(56, UUID(), '持久化 (AOF/RDB)',        'database', 54,   2, 'RDB快照、AOF日志、混合持久化'),
(57, UUID(), '主从/哨兵/集群',          'database', 54,   2, '主从复制、哨兵选主、Cluster分片'),
(58, UUID(), '缓存穿透/击穿/雪崩',     'database', 54,   2, '布隆过滤器、互斥锁、过期策略'),
(59, UUID(), '分布式锁',                'database', 54,   2, 'SETNX、Redisson、RedLock'),
(60, UUID(), '数据库设计',              'database', 46,   1, '数据库建模与设计'),
(61, UUID(), '范式与反范式',            'database', 60,   2, '1NF/2NF/3NF、反范式化场景'),
(62, UUID(), '数据库选型',              'database', 60,   2, 'OLTP vs OLAP、SQL vs NoSQL'),
(63, UUID(), '数据迁移',                'database', 60,   2, '在线DDL、双写方案、数据一致性校验'),
(64, UUID(), 'ElasticSearch',           'database', 46,   1, '全文检索、倒排索引、DSL查询'),
(65, UUID(), 'MongoDB',                 'database', 46,   1, '文档数据库、聚合框架、分片');

-- ========================
-- Domain: system-design — 系统设计 (15 nodes, id 66-80)
-- ========================

INSERT INTO knowledge_nodes (id, uuid, name, domain, parent_id, depth, description) VALUES
(66, UUID(), '系统设计',                'system-design', NULL, 0, '系统设计核心知识体系'),
(67, UUID(), '负载均衡策略',            'system-design', 66,   1, 'Round Robin、加权、一致性哈希'),
(68, UUID(), '缓存策略',                'system-design', 66,   1, 'Cache Aside、Read/Write Through、Write Back'),
(69, UUID(), '限流降级',                'system-design', 66,   1, '令牌桶、漏桶、滑动窗口'),
(70, UUID(), '高可用设计',              'system-design', 66,   1, '主备、双活、异地多活'),
(71, UUID(), 'CDN',                     'system-design', 66,   1, '内容分发网络、回源策略、缓存刷新'),
(72, UUID(), 'API设计',                 'system-design', 66,   1, 'RESTful、GraphQL、版本控制、幂等性'),
(73, UUID(), '监控与告警',              'system-design', 66,   1, 'Prometheus、Grafana、链路追踪'),
(74, UUID(), '日志系统',                'system-design', 66,   1, 'ELK Stack、日志分级、结构化日志'),
(75, UUID(), '经典场景',                'system-design', 66,   1, '常见系统设计面试题'),
(76, UUID(), '短链系统',                'system-design', 75,   2, '哈希 vs 自增ID、302重定向、统计分析'),
(77, UUID(), '秒杀系统',                'system-design', 75,   2, '流量削峰、库存扣减、防超卖'),
(78, UUID(), '即时通讯',                'system-design', 75,   2, 'WebSocket、消息存储、已读/未读'),
(79, UUID(), '新闻Feed流',              'system-design', 75,   2, '推模式 vs 拉模式、Timeline排序'),
(80, UUID(), '搜索引擎',                'system-design', 75,   2, '爬虫、倒排索引、PageRank');

-- ============================================================
-- 知识图谱边关系 (prerequisite / related / contains)
-- ============================================================

INSERT INTO knowledge_edges (uuid, source_id, target_id, relation_type, weight) VALUES
-- Java: 前置依赖关系
(UUID(), 3,  4,  'prerequisite', 1.0),   -- 内存模型 -> GC
(UUID(), 3,  7,  'prerequisite', 1.0),   -- 内存模型 -> JVM调优
(UUID(), 4,  7,  'prerequisite', 1.0),   -- GC -> JVM调优
(UUID(), 11, 12, 'prerequisite', 1.0),   -- volatile -> CAS
(UUID(), 12, 13, 'prerequisite', 1.0),   -- CAS -> AQS
(UUID(), 13, 10, 'prerequisite', 1.0),   -- AQS -> synchronized vs ReentrantLock
(UUID(), 16, 17, 'prerequisite', 1.0),   -- HashMap -> ConcurrentHashMap
(UUID(), 21, 22, 'prerequisite', 1.0),   -- IoC/DI -> AOP
(UUID(), 23, 24, 'prerequisite', 1.0),   -- Spring MVC -> Spring Boot自动配置
(UUID(), 21, 25, 'prerequisite', 1.0),   -- IoC/DI -> Spring事务管理

-- 分布式: 前置依赖关系
(UUID(), 27, 28, 'prerequisite', 1.0),   -- CAP -> 一致性
(UUID(), 28, 32, 'prerequisite', 1.0),   -- 一致性 -> 分布式事务
(UUID(), 36, 37, 'prerequisite', 1.0),   -- 消息队列 -> Kafka
(UUID(), 36, 38, 'prerequisite', 1.0),   -- 消息队列 -> RabbitMQ
(UUID(), 36, 39, 'prerequisite', 1.0),   -- 消息队列 -> 消息可靠性
(UUID(), 40, 41, 'prerequisite', 1.0),   -- 微服务 -> 服务注册与发现
(UUID(), 40, 43, 'prerequisite', 1.0),   -- 微服务 -> 熔断降级

-- 数据库: 前置依赖关系
(UUID(), 48, 53, 'prerequisite', 1.0),   -- 索引 -> SQL优化
(UUID(), 49, 50, 'prerequisite', 1.0),   -- 事务隔离级别 -> MVCC
(UUID(), 55, 56, 'prerequisite', 1.0),   -- Redis数据结构 -> 持久化
(UUID(), 55, 59, 'prerequisite', 1.0),   -- Redis数据结构 -> 分布式锁
(UUID(), 56, 57, 'prerequisite', 1.0),   -- 持久化 -> 主从/哨兵/集群

-- 跨Domain关联关系
(UUID(), 25, 49, 'related', 0.8),        -- Spring事务 <-> 数据库事务隔离级别
(UUID(), 9,  42, 'related', 0.6),        -- 线程池 <-> 负载均衡
(UUID(), 37, 39, 'related', 0.9),        -- Kafka <-> 消息可靠性
(UUID(), 59, 10, 'related', 0.7),        -- 分布式锁 <-> synchronized vs ReentrantLock
(UUID(), 58, 68, 'related', 0.9),        -- 缓存穿透/击穿/雪崩 <-> 缓存策略
(UUID(), 52, 45, 'related', 0.7),        -- 分库分表 <-> 分布式ID生成
(UUID(), 43, 69, 'related', 0.8),        -- 熔断降级 <-> 限流降级
(UUID(), 51, 57, 'related', 0.7),        -- MySQL主从复制 <-> Redis主从/哨兵/集群
(UUID(), 32, 34, 'related', 0.8),        -- 分布式事务 <-> Saga模式 (already parent-child, but also related)
(UUID(), 64, 80, 'related', 0.9);        -- ElasticSearch <-> 搜索引擎
