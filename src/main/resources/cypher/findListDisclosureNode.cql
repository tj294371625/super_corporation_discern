match p=(start:ent {nodeid: $entId}) <-[:groupparent*1..8]- (end)
where not (:ent) -[:groupparent]-> (end)
and all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))
and apoc.coll.duplicates(nodes(p)) = []
with collect({length: length(p), result: end}) as moreinfo
return moreinfo