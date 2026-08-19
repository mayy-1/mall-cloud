-- 领取优惠券 Lua 原子脚本
-- KEYS[1]: coupon:stock:{couponId}      库存 key
-- KEYS[2]: coupon:users:{couponId}      已领用户 Set
-- ARGV[1]: memberId                     当前会员 ID
-- ARGV[2]: perLimit                     每人限领次数
-- 返回: 1=成功  0=库存不足  -1=已领取过

-- 1. 判断库存
local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock <= 0 then
    return 0
end

-- 2. 判断是否已领取过
local claimed = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if claimed == 1 then
    return -1
end

-- 3. 原子扣库存 + 记录用户
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])

return 1
