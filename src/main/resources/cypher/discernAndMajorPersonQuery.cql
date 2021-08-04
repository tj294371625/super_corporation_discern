match p =(start:ent {nodeid: $nodeid})<-[r1:teninvmerge]-(person:person)-[r3:teninvmerge]->(end:ent)<-[r2:teninvmerge]-(start)
where
0.5 >= toFloat(
      case
      when r1.conprop is not null  then toFloat(r1.conprop)
      when r1.conprop is  null  then  toFloat(r1.holderrto)/100
      end
) >= 0.1
and 0.5 >= toFloat(
 case
      when r2.conprop is not null  then toFloat(r2.conprop)
      when r2.conprop is  null  then  toFloat(r2.holderrto)/100
      end
) > 0.0
and toFloat(
  case
      when r3.conprop is not null then toFloat(r3.conprop)
      when r3.conprop is  null  then  toFloat(r3.holderrto)/100
      end

) + toFloat(
 case
      when r2.conprop is not null then toFloat(r2.conprop)
      when r2.conprop is  null then  toFloat(r2.holderrto)/100
      end
) > 0.5
and (end.nodeid<>start.nodeid)
and 0.5 >= toFloat(
  case
      when r3.conprop is not null then toFloat(r3.conprop)
      when r3.conprop is  null  then  toFloat(r3.holderrto)/100
      end

) > 0.0
and (end.entstatus='1')
unwind nodes(p) as n
unwind relationships(p) as rel
with collect(distinct n) as nodes ,collect(distinct rel) as links
with collect({path: {nodes: nodes, links: links}}) as moreinfo
return moreinfo