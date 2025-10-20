-- 窗口计数+1
local current = redis.call('incr',KEYS[1])

-- 如果是第一次添加，设置过期时间
if current == 1 then
    redis.call('expire',KEYS[1],ARGV[1])
end

return current