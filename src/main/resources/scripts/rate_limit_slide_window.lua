local key = KEYS[1] -- 获取redis Key
local now = tonumber(ARGV[1]) -- 获取当前时间戳
local windowStart = tonumber(ARGV[2]) -- 获取窗口起始时间
local maxRequests = tonumber(ARGV[3]) -- 获取最大请求数
local windowSeconds = tonumber(ARGV[4]) -- 获取窗口大小

-- 移除所有不在当前窗口的元素
redis.call('zremrangebyscore', key,0,windowStart)

-- 获取当前窗口的请求数
local current = redis.call('zcard',key)

-- 判断是否到底最大请求数
if current < maxRequests then
    redis.call('zadd',key,now,now) -- 将请求加入窗口，时间戳表示何时加入的
    redis.call('expire',key, windowSeconds)
    -- 可以请求
    return 1
else
    -- 拒绝请求
    return 0
end