local key = KEYS[1] -- 漏桶的key
local capacity = tonumber(ARGV[1]) -- 漏桶容量
local leakRate = tonumber(ARGV[2]) -- 漏桶速率
local now = tonumber(ARGV[3]) -- 当前时间戳

-- 获取当前桶中的信息，每次请求到来时延时计算桶的容量
local bucket = redis.call('hmget',key,'water','lastLeakTime')
local currentWater = tonumber(bucket[1] or 0) -- 当前水量，默认0
local lastLeakTime = tonumber(bucket[2] or now) -- 上次漏水时间，默认当前时间戳

-- 距离上次漏水，经过了多少秒
local timePassed = (now - lastLeakTime) / 1000
-- 计算这段时间，漏出了多少水
local leakedWater = math.floor(timePassed * leakRate)--不能有半个请求

-- 如果有漏出的水，重新计算容量
if leakedWater > 0 then
    currentWater = math.max(0,currentWater-leakedWater) -- 不能小于0
    lastLeakTime = now -- 漏出时间为当前时间戳
end

-- 检查桶是否有空间容纳新请求
if currentWater < capacity then
    currentWater  = currentWater + 1 -- 新请求入桶
    redis.call('hmset',key,'water',currentWater,'lastLeakTime',lastLeakTime) -- 持久化
    redis.call('expire',key,math.ceil(capacity/leakRate)*2) -- 过期时间
    return 1 -- 允许请求
else
    return 0 -- 桶满
end