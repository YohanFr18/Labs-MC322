# Estruturando a Fábrica

A ideia é criar uma fábrica de processadores em que os produtos são modelos de CPUs diferentes. A diferença entre um
produto e outro, ou seja, entre as CPUs produzidas, será a quantidade de núcleos necessária para cada modelo. Com isso,
podemos estabelecer a nossa matéria-prima como sendo os "wafer", assim, ao invés de trabalharmos com quantos wafers cada
modelo consome, usamos a área de cada wafer. Dessa forma, nossa linha de produtos é, basicamente, uma escala de "tamanho
de chips.".

Os produtos serão 3 modelos de cpus:

- Turing-X4
- Lovelace-X8
- Torvalds-X16

A matéria-prima será:

- Wafer de silício 300 mm de diâmetro
- Unidade em mm2
- Estoque inicial 5000 unidades mm2
- Quantidade mínima 100mm2
