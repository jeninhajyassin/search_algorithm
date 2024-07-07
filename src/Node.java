
class Node implements Comparable<Node> {
    String person;
    int cost;

    public Node(String person, int cost) {
        this.person = person;
        this.cost = cost;
    }

    @Override
    public int compareTo(Node other) {
        return this.cost - other.cost;
    }
}