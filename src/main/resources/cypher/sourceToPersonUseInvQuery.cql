match (start:ent {nodeid: $startId}), (end:person {zsid: $endId})
call apoc.path.expandConfig(start, {terminatorNodes: end, maxLevel: $layer, bfs:true, relationshipFilter: '<teninvmerge'})
yield path with path as p, start, end
  where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))
  and all(r in relationships(p) where (toFloat(r.conprop) > 0) or toFloat(r.holderrto) > 0)
  and apoc.coll.duplicates(nodes(p)) = []
with p, start, end, relationships(p) as inv

with id(end) as endId, collect(p) as path,
     reduce(final=0.0,e in collect(
     reduce(onePath=1.0, n IN inv | toFloat(onePath) * (
     case when n.conprop is not null then toFloat(n.conprop) else  toFloat(n.holderrto)/100 end
     )
     ))  |  final+e ) as finalResult
with endId, path, finalResult,
     reduce(final = [], p in path | final + nodes(p)) as nodes,
     reduce(final = [], p in path | final + relationships(p)) as links
with collect({cgzb: finalResult, path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo