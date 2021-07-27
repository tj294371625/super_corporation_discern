match p = (start:ent {nodeid: $entId}) <-[inv:teninvmerge*1..8]- (end)
where all(n in nodes(p) where (n.entstatus is null or n.entstatus = '1'))
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