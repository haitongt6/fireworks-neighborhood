-- 短信限流 Lua 脚本（原子执行）
-- KEYS[1] = sms:limit:interval:{phone}  （60秒间隔限制）
-- KEYS[2] = sms:limit:daily:{phone}    （每日总量限制）
-- ARGV[1] = 60  间隔秒数
-- ARGV[2] = 86400  每日 key 过期秒数（24小时）
--
-- 返回值：
--   1  成功，可发送
--  -1  60秒内已发送，请稍后再试
--  -2  今日已达上限（10次）

local interval_exists = redis.call('EXISTS', KEYS[1])
if interval_exists == 1 then
    return -1
end

local daily_count = redis.call('GET', KEYS[2])
if daily_count and tonumber(daily_count) >= 10 then
    return -2
end

redis.call('SETEX', KEYS[1], ARGV[1], '1')
local new_daily = redis.call('INCR', KEYS[2])
if new_daily == 1 then
    redis.call('EXPIRE', KEYS[2], ARGV[2])
end
return 1
