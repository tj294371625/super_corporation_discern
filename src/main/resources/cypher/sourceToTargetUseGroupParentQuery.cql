MATCH p=(start:ent {nodeid: $startId})<-[inv:groupparent*1..8]-(end:ent {nodeid: $endId})
where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))
and apoc.coll.duplicates(nodes(p)) = []
with p, start, end, relationships(p) as inv
with id(end) as endId, collect(p) as path,
    reduce(final=0.0,e in collect(
    reduce(onePath=1.0, n IN inv | toFloat(onePath) * (
      case when n.conprop is not null then toFloat(n.conprop)
      when n.holderrto is not null then toFloat(n.holderrto)/100
      else 0.0 end
      )
    ))  |  final+e ) as finalResult
with endId, path, finalResult,
reduce(final = [], p in path | final + nodes(p)) as nodes,
reduce(final = [], p in path | final + relationships(p)) as links
with collect({cgzb: finalResult, path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo