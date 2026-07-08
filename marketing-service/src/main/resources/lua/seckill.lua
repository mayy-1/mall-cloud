-- 秒杀 Lua 原子脚本
-- 功能：单条 Lua 完成三步原子操作，杜绝超卖
-- KEYS[1]: seckill:stock:{promotionId}:{productId}  库存 key
-- KEYS[2]: seckill:users:{promotionId}:{productId}  已购买用户 Set
-- ARGV[1]: memberId                                  当前用户ID
-- ARGV[2]: limitPerUser                              每人限购数量（暂用 Set 去重保证每人仅一次）
-- 返回值:
--   1  -> 秒杀成功
--   0  -> 库存不足
--  -1  -> 用户已购买过（重复秒杀）

-- 1. 判断用户是否已购买（Set 去重）
local purchased = redis.call('sismember', KEYS[2], ARGV[1])
if purchased == 1 then
    return -1
end

-- 2. 判断库存是否充足
local stock = tonumber(redis.call('get', KEYS[1]) or "0")
if stock <= 0 then
    return 0
end

-- 3. 原子扣减库存并记录用户
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])

return 1
