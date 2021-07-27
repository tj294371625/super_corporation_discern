match p = (start:ent {nodeid: $entId}) <-[inv:teninvmerge*1..8]- (end:ent)
where (end.type <> '108' or end.type is null)
and all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))
and ( not end.name in ['中国投资有限责任公司','中央汇金投资有限责任公司','国务院','中央结算代理中心'])
and all(n in nodes(p) where not (n.name =~ '.*国有资产.*'))
and all(n in nodes(p) where not (n.name =~ '.*国有资本经营管理中心'))
and all(n in nodes(p) where not (n.name ='梧桐树投资平台有限责任公司'))
and all(n in nodes(p) where not (n.name =~ '.*国有资本运营.*'))
and apoc.coll.duplicates(nodes(p)) = []
with end, collect(length(p)) as pathLengthRow,
    reduce(final = 0.0, e in collect(
        reduce(onePath = 1.0, n IN inv | toFloat(onePath) * (
            case
            when n.sc_conprop is not null then toFloat(n.sc_conprop)
            else toFloat(n.sc_holderrto) / 100
            end
        )
    ))  |  final+e) as prop
where prop > 0.5
with end, pathLengthRow, prop
unwind pathLengthRow as pathLengthColumn
with min(pathLengthColumn) as shortestPathLength, end as result, prop as cgzb
with collect({length: shortestPathLength, cgzb: cgzb, result: result}) as moreinfo
return moreinfo