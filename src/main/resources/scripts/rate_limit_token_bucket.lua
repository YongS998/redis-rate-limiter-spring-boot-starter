-- 获取需求令牌桶标识
local key = KEYS[1]
local tokensRequested = tonumber(ARGV[1]) --本次请求需要的token数
local capacity = tonumber(ARGV[2]) --令牌桶容量
local refillRate = tonumber(ARGV[3]) --填充令牌速率
local now = tonumber(ARGV[4]) --当前时间戳

--获取令牌桶中令牌数量和上次填充时间
local bucket = redis.call('hmget',key,'tokens','lastRefill')
local currentTokens = tonumber(bucket[1] or capacity) --默认为满token
local lastRefill = tonumber(bucket[2] or now) --默认为当前

-- 计算距离上次填充经过了多少秒
local timePassed = (now-lastRefill) / 1000
-- 计算要补充的令牌数
local refillTokens = math.floor(refillRate * timePassed)

-- 如果有补充的令牌，则更新桶
if refillTokens > 0 then
    -- 不能超过容量
    currentTokens = math.min(capacity,currentTokens + refillTokens)
    lastRefill = now
end

-- 检查当前令牌是否能供给给本次请求
if currentTokens >= tokensRequested then
    currentTokens = currentTokens - tokensRequested --扣除令牌
    --桶状态持久化
    redis.call('hmset',key,'tokens',currentTokens,'lastRefill',lastRefill)
    -- 设置过期时间
    redis.call('expire',key,math.ceil(capacity/refillRate)*2)
    return 1
else
    return 0
end