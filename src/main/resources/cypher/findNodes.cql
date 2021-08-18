match (e:ent)
where e.nodeid in $entIds
with collect(distinct e) as nodes
with collect({nodes: nodes}) as moreinfo
return moreinfo