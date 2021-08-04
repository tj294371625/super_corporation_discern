match p =(start:ent{nodeid:$nodeid})<-[r1:teninvmerge]-(person:person)
where
0.5 > toFloat(
      case
      when r1.conprop is not null  then toFloat(r1.conprop)
      when r1.conprop is  null then  toFloat(r1.holderrto)/100
      end
) >= 0.1
unwind nodes(p) as n
unwind relationships(p) as rel
with collect(distinct n) as nodes, collect(distinct rel) as links
with collect({path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo