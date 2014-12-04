mongodb orm
mongodb 的简单封装

MongoBaseDAO<Order> dao = MongoBaseDAO.instanceOf(Order.class);
Order order = new Order();
// set prop
dao.insert(order);
