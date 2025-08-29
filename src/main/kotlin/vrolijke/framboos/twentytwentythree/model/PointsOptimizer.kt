package vrolijke.framboos.twentytwentythree.model

inline fun optimizePoints(points: MutableList<Point>): MutableList<Point> {
    val uniquePoints = mutableListOf<Point>()

    var skipper = -1;

    for ((index, point) in points.withIndex()) {
        if (index < skipper) continue

        uniquePoints.add(point)

        for (i in index + 1 until points.size) {
            if (point == points.getOrNull(i)) {
                skipper = i + 1
            }
        }
    }

    return uniquePoints
}


// optimize
// [Point(x=1, y=8), Point(x=0, y=8), Point(x=1, y=8), Point(x=1, y=7), Point(x=2, y=7), Point(x=3, y=7), Point(x=3, y=6),
// Point(x=2, y=6), Point(x=1, y=6), Point(x=2, y=6), Point(x=3, y=6), Point(x=3, y=5), Point(x=3, y=6), Point(x=4, y=6),
// Point(x=5, y=6), Point(x=5, y=5)]


// [Point(x=1, y=8), Point(x=1, y=7), Point(x=2, y=7), Point(x=3, y=7), Point(x=3, y=6),
// Point(x=2, y=6), Point(x=1, y=6), Point(x=2, y=6), Point(x=3, y=6), Point(x=3, y=5), Point(x=3, y=6), Point(x=4, y=6),
// Point(x=5, y=6), Point(x=5, y=5)]



fun main() {
    val points = mutableListOf(
        Point(x=1, y=2), Point(x=1, y=1), Point(x=0, y=1), Point(x=0, y=2), Point(x=0, y=3), Point(x=0, y=4), Point(x=1, y=4), Point(x=1, y=3), Point(x=1, y=4), Point(x=0, y=4), Point(x=0, y=5), Point(x=1, y=5), Point(x=2, y=5), Point(x=2, y=4), Point(x=2, y=5), Point(x=3, y=5), Point(x=3, y=4), Point(x=4, y=4), Point(x=4, y=3), Point(x=3, y=3), Point(x=3, y=2), Point(x=3, y=1), Point(x=2, y=1), Point(x=2, y=2), Point(x=2, y=3), Point(x=2, y=2), Point(x=2, y=1), Point(x=2, y=0), Point(x=2, y=1), Point(x=3, y=1), Point(x=3, y=0), Point(x=4, y=0), Point(x=4, y=1), Point(x=5, y=1), Point(x=5, y=0), Point(x=6, y=0), Point(x=7, y=0), Point(x=7, y=1), Point(x=7, y=0), Point(x=6, y=0), Point(x=5, y=0), Point(x=5, y=1), Point(x=4, y=1), Point(x=4, y=2), Point(x=4, y=1), Point(x=4, y=0), Point(x=3, y=0), Point(x=3, y=1), Point(x=3, y=2), Point(x=3, y=3), Point(x=4, y=3), Point(x=5, y=3), Point(x=5, y=2), Point(x=6, y=2), Point(x=6, y=1), Point(x=6, y=2), Point(x=7, y=2), Point(x=8, y=2), Point(x=8, y=3), Point(x=9, y=3), Point(x=9, y=2), Point(x=9, y=1), Point(x=8, y=1), Point(x=9, y=1), Point(x=9, y=0), Point(x=8, y=0), Point(x=9, y=0), Point(x=9, y=1), Point(x=9, y=2), Point(x=9, y=3), Point(x=8, y=3), Point(x=8, y=2), Point(x=7, y=2), Point(x=6, y=2), Point(x=6, y=3), Point(x=6, y=2), Point(x=5, y=2), Point(x=5, y=3), Point(x=4, y=3), Point(x=4, y=4), Point(x=5, y=4), Point(x=6, y=4), Point(x=7, y=4), Point(x=7, y=3), Point(x=7, y=4), Point(x=8, y=4), Point(x=8, y=5), Point(x=9, y=5), Point(x=9, y=4), Point(x=9, y=5), Point(x=9, y=6), Point(x=9, y=5), Point(x=8, y=5), Point(x=8, y=4), Point(x=7, y=4), Point(x=7, y=5), Point(x=6, y=5), Point(x=7, y=5), Point(x=7, y=4), Point(x=6, y=4), Point(x=5, y=4), Point(x=4, y=4), Point(x=4, y=5), Point(x=5, y=5)

    )

    val uniquePoints = optimizePoints(points)

    //println(points.size)
    //println(uniquePoints.size)
    //println(uniquePoints)
}
