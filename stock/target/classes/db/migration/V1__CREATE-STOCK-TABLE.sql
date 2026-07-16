CREATE TABLE stock(
  id SERIAL PRIMARY KEY,
  product VARCHAR(255),
  quantity INT,
  price NUMERIC(19, 2)
);