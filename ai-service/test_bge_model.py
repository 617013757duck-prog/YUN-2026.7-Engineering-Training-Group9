"""
测试BGE模型加载
"""
from sentence_transformers import SentenceTransformer

print("加载BGE模型...")
model = SentenceTransformer('BAAI/bge-small-zh-v1.5')

print("✅ 模型加载成功!")

# 测试编码
print("\n测试文本编码...")
texts = [
    "头痛发热",
    "患者主诉胸痛三天",
    "症状分析结果"
]

embeddings = model.encode(texts)
print(f"向量维度: {embeddings.shape}")
print(f"编码文本数: {len(texts)}")

print("\n✅ BGE模型测试通过!")
print(f"模型可以正常使用，维度为 {embeddings.shape[1]}")