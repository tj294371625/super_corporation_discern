match p =(e:ent{nodeid:$nodeid})<-[r2:staff]-(person:person)
where (r2.position in $staffList)
unwind nodes(p) as n
unwind relationships(p) as rel
with collect(distinct n) as nodes ,collect(distinct rel) as links
with collect({path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo