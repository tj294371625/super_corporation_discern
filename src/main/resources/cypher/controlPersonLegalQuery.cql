match p =(e:ent)<-[r:legal]-(per:person{zsid:$zsid})
where e.nodeid <> $entid
unwind nodes(p) as n
unwind relationships(p) as rel
with collect(distinct n) as nodes, collect(distinct rel) as links
with collect({path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo