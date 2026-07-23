"""
下载BGE嵌入模型脚本
"""
import os
from sentence_transformers import SentenceTransformer

# 配置Hugging Face镜像（国内）
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'

# 模型名称
MODEL_NAME = 'BAAI/bge-small-zh-v1.5'

print(f"开始下载模型: {MODEL_NAME}")
print("使用镜像站点: https://hf-mirror.com")
print("=" * 60)

try:
    # 下载并加载模型
    model = SentenceTransformer(MODEL_NAME)
    print(f"\n✅ 模型下载成功!")
    print(f"模型路径: {model._model_card_vars['model_name']}")
    
    # 测试模型
    print("\n测试模型...")
    test_embedding = model.encode("测试文本")
    print(f"向量维度: {test_embedding.shape}")
    print("✅ 模型测试通过!")
    
except Exception as e:
    print(f"\n❌ 模型下载失败: {e}")
    print("\n建议解决方案:")
    print("1. 配置网络代理（推荐）")
    print("   set HTTP_PROXY=http://127.0.0.1:7890")
    print("   set HTTPS_PROXY=http://127.0.0.1:7890")
    print("2. 或手动下载模型")
    print("3. 或联系AI组员获取模型文件")