match p =(e:ent{nodeid:$nodeid})<-[r2:staff]-(person:person)
where (r2.position in $staffList)
unwind nodes(p) as n
unwind relationships(p) as rel
with collect({path: {nodes: n, links: rel}}) as moreinfo
return moreinfo